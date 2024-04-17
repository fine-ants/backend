package codesquad.fineants.spring.api.stock_dividend.service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
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
		List<Dividend> dividends = amazonS3DividendService.fetchDividend();
		// 기존 종목 배당금 데이터 삭제
		stockDividendRepository.deleteAllInBatch();
		// S3에 저장된 종목 배당금으로 초기화
		Map<String, Stock> stockMap = stockRepository.findAll().stream()
			.collect(Collectors.toMap(Stock::getTickerSymbol, stock -> stock));

		List<StockDividend> stockDividends = new ArrayList<>();
		for (Dividend dividend : dividends) {
			if (dividend.containsBy(stockMap)) {
				Stock stock = dividend.getStockBy(stockMap);
				StockDividend stockDividend = dividend.toEntity(stock);
				stockDividends.add(stockDividend);
			}
		}
		List<StockDividend> saveStockDividends = stockDividendRepository.saveAll(stockDividends);
		log.info("save StockDividends size : {}", saveStockDividends.size());
	}

	/**
	 * 배당 일정 최신화
	 * - 새로운 배당 일정 추가
	 * - 현금 지급일 수정
	 * - 범위를 벗어난 배당 일정 삭제
	 */
	@Transactional
	public void refreshStockDividend(LocalDate now) {
		// 1. 새로운 배당 일정 추가
		// 올해 말까지의 배당 일정을 조회
		LocalDate to = now.with(TemporalAdjusters.lastDayOfYear());
		List<KisDividend> kisDividends = kisService.fetchDividendAll(now, to);

		// 기존 데이터와 비교하여 새롭게 추가된 배당 일정 탐색
		List<String> tickerSymbols = kisDividends.stream()
			.map(KisDividend::getTickerSymbol)
			.collect(Collectors.toList());
		Map<String, Stock> stockMap = stockRepository.findAllWithDividends(tickerSymbols)
			.stream()
			.collect(Collectors.toMap(Stock::getTickerSymbol, stock -> stock));

		// 추가된 배당 일정 탐색
		List<StockDividend> addStockDividends = kisDividends.stream()
			.filter(kisDividend -> kisDividend.containsFrom(stockMap))
			.filter(kisDividend -> !kisDividend.matchTickerSymbolAndRecordDateFrom(stockMap))
			.map(kisDividend -> kisDividend.toEntity(kisDividend.getStockBy(stockMap)))
			.collect(Collectors.toList());

		// 탐색된 데이터들 배당 일정 추가
		log.debug("addStockDividends : {}", addStockDividends);
		stockDividendRepository.saveAll(addStockDividends);

		// 2. 현금 지급일 수정
		// 현금 지급일을 가지고 있지 않은 배당 일정 조회
		List<StockDividend> changedStockDividends = kisDividends.stream()
			.filter(kisDividend -> kisDividend.containsFrom(stockMap))
			.filter(kisDividend -> kisDividend.matchTickerSymbolAndRecordDateFrom(stockMap))
			.map(kisDividend -> {
				StockDividend stockDividend = kisDividend.getStockDividendByTickerSymbolAndRecordDateFrom(stockMap)
					.orElse(null);
				if (stockDividend == null) {
					return null;
				}
				if (stockDividend.hasPaymentDate()) {
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
		log.debug("changedStockDividends : {}", changedStockDividends);
		stockDividendRepository.saveAll(changedStockDividends);

		// 범위를 벗어난 배당 일정을 삭제
		LocalDate from = now.minusYears(1L).with(TemporalAdjusters.firstDayOfYear());
		List<StockDividend> deleteStockDividends = stockMap.values().stream()
			.map(stock -> stock.getStockDividendNotInRange(from, to))
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
		log.debug("deleteStockDividends : {}", deleteStockDividends);
		stockDividendRepository.deleteAllInBatch(deleteStockDividends);
	}

}
