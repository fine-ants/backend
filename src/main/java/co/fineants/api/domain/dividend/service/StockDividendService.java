package co.fineants.api.domain.dividend.service;

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

import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.kis.domain.dto.response.KisDividend;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.infra.s3.service.AmazonS3DividendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDividendService {

	private final AmazonS3DividendService s3DividendService;
	private final StockRepository stockRepository;
	private final StockDividendRepository stockDividendRepository;
	private final KisService kisService;
	private final LocalDateTimeService localDateTimeService;
	private final ExDividendDateCalculator exDividendDateCalculator;

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
		List<StockDividend> stockDividends = s3DividendService.fetchDividends();
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
		List<KisDividend> kisDividends = kisService.fetchDividendsBetween(now, to);

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
				StockDividend changeStockDividend = kisDividend.toEntity(kisDividend.getStockBy(stockMap),
					exDividendDateCalculator);
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
			.map(kisDividend -> kisDividend.toEntity(kisDividend.getStockBy(stockMap), exDividendDateCalculator))
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

	@Transactional(readOnly = true)
	public List<StockDividend> findAllStockDividends() {
		return stockDividendRepository.findAllStockDividends();
	}
}
