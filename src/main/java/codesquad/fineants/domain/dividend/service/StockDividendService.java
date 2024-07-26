package codesquad.fineants.domain.dividend.service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.dividend.repository.StockDividendRepository;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividend;
import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.global.common.time.LocalDateTimeService;
import codesquad.fineants.infra.s3.dto.Dividend;
import codesquad.fineants.infra.s3.service.AmazonS3DividendService;
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
	private final LocalDateTimeService localDateTimeService;

	/**
	 * 배당일정(StockDividend) 엔티티 데이터를 초기화합니다.
	 * - 기존 배당 일정은 제거
	 * - S3로부터 배당 일정 파일(csv)을 기반으로 초기화 수행
	 * - 이 메서드는 서버 시작시 수행됨
	 */
	@Transactional
	@Secured("ROLE_ADMIN")
	public void initializeStockDividend() {
		// 기존 종목 배당금 데이터 삭제
		stockDividendRepository.deleteAllInBatch();

		// S3에 저장된 종목 배당금으로 초기화
		initializeStockDividendFromS3();
	}

	private void initializeStockDividendFromS3() {
		// 종목 조회
		Map<String, Stock> stockMap = stockRepository.findAll().stream()
			.collect(Collectors.toMap(Stock::getTickerSymbol, stock -> stock));

		// 종목에 해당하는 배당 일정 엔티티 생성
		List<StockDividend> stockDividends = amazonS3DividendService.fetchDividend().stream()
			.filter(dividend -> dividend.containsBy(stockMap))
			.map(dividend -> {
				Stock stock = dividend.getStockBy(stockMap);
				return dividend.toEntity(stock);
			})
			.toList();

		// 배당 일정 저장
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
	public void reloadStockDividend() {
		// 0. 올해 말까지의 배당 일정을 조회
		LocalDate now = localDateTimeService.getLocalDateWithNow();
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
			.toList();
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
			.toList();
		log.info("changedStockDividends : {}", changedStockDividends);
		stockDividendRepository.saveAll(changedStockDividends);
	}

	private void addNewStockDividend(List<KisDividend> kisDividends, Map<String, Stock> stockMap) {
		// 추가된 배당 일정 탐색
		List<StockDividend> addStockDividends = kisDividends.stream()
			.filter(kisDividend -> kisDividend.containsFrom(stockMap))
			.filter(kisDividend -> !kisDividend.matchTickerSymbolAndRecordDateFrom(stockMap))
			.map(kisDividend -> kisDividend.toEntity(kisDividend.getStockBy(stockMap)))
			.toList();

		// 탐색된 데이터들 배당 일정 추가
		stockDividendRepository.saveAll(addStockDividends);
		log.info("addStockDividends : {}", addStockDividends);
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
			.toList();
		stockDividendRepository.deleteAllInBatch(deleteStockDividends);
		log.info("deleteStockDividends : {}", deleteStockDividends);
	}

	/**
	 * 데이터베이스의 배당 일정을 S3에 CSV 파일로 저장
	 */
	@Transactional(readOnly = true)
	public void writeDividendCsvToS3() {
		List<Dividend> dividends = stockDividendRepository.findAllStockDividends().stream()
			.map(StockDividend::toDividend)
			.toList();
		amazonS3DividendService.writeDividend(dividends);
		log.info("write dividend csv to s3, size={}", dividends.size());
	}
}
