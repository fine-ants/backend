package codesquad.fineants.spring.api.stock_dividend.service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.domain.stock_dividend.StockDividendRepository;
import codesquad.fineants.spring.api.S3.dto.Dividend;
import codesquad.fineants.spring.api.S3.service.AmazonS3DividendService;
import codesquad.fineants.spring.api.kis.response.KisDividend;
import codesquad.fineants.spring.api.kis.service.KisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDividendService {

	private final AmazonS3DividendService amazonS3DividendService;
	private final StockRepository stockRepository;
	private final StockDividendRepository stockDividendRepository;
	private final KisService kisService;

	@Transactional
	public void initStockDividend() {
		// 기존 종목 배당금 데이터 삭제
		stockDividendRepository.deleteAllInBatch();

		Map<String, Stock> stockMap = stockRepository.findAll().stream()
			.collect(Collectors.toMap(Stock::getTickerSymbol, stock -> stock));

		// S3에 저장된 종목 배당금으로 초기화
		List<StockDividend> stockDividends = amazonS3DividendService.fetchDividend().stream()
			.filter(dividend -> dividend.containsBy(stockMap))
			.map(dividend -> {
				Stock stock = dividend.getStockBy(stockMap);
				return dividend.toEntity(stock);
			})
			.collect(Collectors.toList());
		List<StockDividend> saveStockDividends = stockDividendRepository.saveAll(stockDividends);
		log.info("save StockDividends size : {}", saveStockDividends.size());
	}

	/**
	 * 배당 일정 최신화
	 * - 새로운 배당 일정 추가
	 * - 현금 지급일 수정
	 * - 범위를 벗어난 배당 일정 삭제
	 *   - ex) now=202404-17 => 범위를 벗어난 배당 일정은 2023-01-01 이전 or 2024-12-31 이후
	 */
	@Transactional
	public void refreshStockDividend(LocalDate now) {
		// 0. 올해 말까지의 배당 일정을 조회
		LocalDate to = now.with(TemporalAdjusters.lastDayOfYear());
		List<KisDividend> kisDividends = kisService.fetchDividendAll(now, to);

		// 1. 새로운 배당 일정 탐색
		Map<String, Stock> stockMap = getStockMapBy(kisDividends);

		// 3. 새로운 배당 일정 추가
		addNewStockDividend(kisDividends, stockMap);

		// 4. 현금 지급일 수정
		updateStockDividendWithPaymentDate(kisDividends, stockMap);

		// 5. 범위를 벗어난 배당 일정을 삭제
		deleteStockDividendNotInRange(now, stockMap);
	}

	private Map<String, Stock> getStockMapBy(List<KisDividend> kisDividends) {
		List<String> tickerSymbols = kisDividends.stream()
			.map(KisDividend::getTickerSymbol)
			.collect(Collectors.toList());
		return stockRepository.findAllWithDividends(tickerSymbols)
			.stream()
			.collect(Collectors.toMap(Stock::getTickerSymbol, stock -> stock));
	}

	private void updateStockDividendWithPaymentDate(List<KisDividend> kisDividends, Map<String, Stock> stockMap) {
		// 현금 지급일을 가지고 있지 않은 배당 일정 조회
		List<StockDividend> changedStockDividends = kisDividends.stream()
			.filter(kisDividend -> kisDividend.containsFrom(stockMap))
			.filter(kisDividend -> kisDividend.matchTickerSymbolAndRecordDateFrom(stockMap))
			.map(kisDividend -> {
				StockDividend stockDividend = kisDividend.getStockDividendByTickerSymbolAndRecordDateFrom(stockMap)
					.orElse(null);
				if (stockDividend == null || stockDividend.hasPaymentDate()) {
					return null;
				}
				StockDividend changeStockDividend = kisDividend.toEntity(kisDividend.getStockBy(stockMap));
				if (!changeStockDividend.hasPaymentDate()) {
					return null;
				}
				stockDividend.change(changeStockDividend);
				return stockDividend;
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		log.info("changedStockDividends : {}", changedStockDividends);
		stockDividendRepository.saveAll(changedStockDividends);
	}

	private void addNewStockDividend(List<KisDividend> kisDividends, Map<String, Stock> stockMap) {
		// 추가된 배당 일정 탐색
		List<StockDividend> addStockDividends = kisDividends.stream()
			.filter(kisDividend -> kisDividend.containsFrom(stockMap))
			.filter(kisDividend -> !kisDividend.matchTickerSymbolAndRecordDateFrom(stockMap))
			.map(kisDividend -> kisDividend.toEntity(kisDividend.getStockBy(stockMap)))
			.collect(Collectors.toList());

		// 탐색된 데이터들 배당 일정 추가
		log.info("addStockDividends : {}", addStockDividends);
		stockDividendRepository.saveAll(addStockDividends);
	}

	/**
	 * 작년 1월 1일부터 올해 12월 31일까지 범위에 없는 배당 일정을 제거
	 * @param now 올해의 기준일자
	 * @param stockMap 종목 맵
	 */
	private void deleteStockDividendNotInRange(LocalDate now, Map<String, Stock> stockMap) {
		int lastYear = 1;
		LocalDate from = now.minusYears(lastYear).with(TemporalAdjusters.firstDayOfYear());
		LocalDate to = now.with(TemporalAdjusters.lastDayOfYear());
		List<StockDividend> deleteStockDividends = stockMap.values().stream()
			.map(stock -> stock.getStockDividendNotInRange(from, to))
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
		log.info("deleteStockDividends : {}", deleteStockDividends);
		stockDividendRepository.deleteAllInBatch(deleteStockDividends);
	}

	@Transactional(readOnly = true)
	public void writeDividendCsvToS3() {
		List<Dividend> dividends = stockDividendRepository.findAllStockDividends().stream()
			.map(StockDividend::toDividend)
			.collect(Collectors.toList());
		amazonS3DividendService.writeDividend(dividends);
	}
}
