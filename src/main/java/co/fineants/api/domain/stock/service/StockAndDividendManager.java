package co.fineants.api.domain.stock.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.kis.domain.dto.response.DividendItem;
import co.fineants.api.domain.kis.domain.dto.response.KisDividend;
import co.fineants.api.domain.kis.domain.dto.response.KisSearchStockInfo;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.stock.domain.dto.response.StockDataResponse;
import co.fineants.api.domain.stock.domain.dto.response.StockReloadResponse;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.errors.errorcode.StockErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockAndDividendManager {
	private final StockRepository stockRepository;
	private final StockDividendRepository dividendRepository;
	private final KisService kisService;
	private final DelayManager delayManager;
	private final ExDividendDateCalculator exDividendDateCalculator;

	/**
	 * 한국 투자 증권 서버에 조회 후 종목 및 배당 일정 최신화 수행
	 * 종목 및 배당 일정 최신화 내용은 다음과 같다
	 * - 신규 상장 종목 저장
	 * - 상장 폐지 종목 및 종목의 배당 일정 삭제
	 *  - 삭제 처리는 소프트 삭제 처리
	 * - 올해 신규 배당 일정 저장
	 * @return StockRefreshResponse - 신규 상장 종목, 상장 페지 종목, 올해 신규 배당 일정
	 */
	@Transactional
	public StockReloadResponse reloadStocks() {
		// 신규 상장 종목 저장
		Set<String> ipoTickerSymbols = saveIpoStocks();

		// 상장 폐지 종목 조회
		Map<Boolean, List<Stock>> partitionedStocksForDelisted = fetchPartitionedStocksForDelisted()
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyMap);

		// 상장 폐지 종목 및 종목의 배당 일정 삭제
		Set<String> deletedStocks = deleteStocks(partitionedStocksForDelisted.get(true));

		// 올해 신규 배당 일정 저장
		Set<DividendItem> addedDividends = reloadDividends(partitionedStocksForDelisted.get(false));

		return StockReloadResponse.create(ipoTickerSymbols, deletedStocks, addedDividends);
	}

	@NotNull
	private Set<DividendItem> reloadDividends(List<Stock> stocks) {
		return Stream.of(stocks)
			.map(this::mapTickerSymbols)
			.map(this::reloadDividend)
			.map(this::mapDividendItems)
			.flatMap(Collection::stream)
			.collect(Collectors.toUnmodifiableSet());
	}

	@NotNull
	private Set<String> deleteStocks(List<Stock> stocks) {
		return Stream.of(stocks)
			.map(this::mapTickerSymbols)
			.map(this::deleteStocks)
			.flatMap(Collection::stream)
			.collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * 신규 상장 종목 저장
	 * 수행과정은 다음과 같습니다.
	 * - 신규 상장 종목 조회
	 * - 신규 상장 종목 저장
	 * @return 신규 상장 종목 티커 심볼
	 */
	@NotNull
	private Set<String> saveIpoStocks() {
		List<Stock> stocks = kisService.fetchStockInfoInRangedIpo()
			.map(StockDataResponse.StockIntegrationInfo::toEntity)
			.onErrorResume(throwable -> {
				log.error("fetchStockInfoInRangedIpo error message is {}", throwable.getMessage());
				return Mono.empty();
			})
			.collectList()
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyList);

		return stockRepository.saveAll(stocks).stream()
			.peek(stock -> log.info("save ipoStock is {}", stock))
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
	}

	private Set<DividendItem> mapDividendItems(List<StockDividend> stockDividends) {
		return stockDividends.stream()
			.map(DividendItem::from)
			.collect(Collectors.toUnmodifiableSet());
	}

	@NotNull
	private Set<String> mapTickerSymbols(List<Stock> stocks) {
		return stocks.stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * 종목 삭제
	 * 종목 삭제시 종목에 포함된 배당 일정을 사전에 제거해야 한다
	 * @param tickerSymbols 삭제할 종목의 티커 심볼
	 */
	private Set<String> deleteStocks(Set<String> tickerSymbols) {
		// 종목의 배당금 삭제
		int deletedDividendCount = dividendRepository.deleteByTickerSymbols(tickerSymbols);
		log.info("delete dividends for TickerSymbols : {}, deleteCount={}", tickerSymbols, deletedDividendCount);

		// 종목 삭제
		int deletedStockCount = stockRepository.deleteAllByTickerSymbols(tickerSymbols);
		log.info("delete stocks for TickerSymbols : {}, deleteCount={}", tickerSymbols, deletedStockCount);

		return tickerSymbols;
	}

	/**
	 * 상장 폐지 종목 조회
	 * 수행 과정
	 * - 종목 티커 전체 조회
	 * - 한국 투자 증권에 종목 조회
	 * - 상장 폐지 종목 분할하여 반환
	 * @return 상장 폐지 종목 분할 맵
	 */
	private Mono<Map<Boolean, List<Stock>>> fetchPartitionedStocksForDelisted() {
		final int concurrency = 20;
		return Flux.fromIterable(findAllTickerSymbols())
			.flatMap(kisService::fetchSearchStockInfo, concurrency)
			.delayElements(delayManager.delay())
			.onErrorResume(throwable -> {
				log.error("fetchPartitionedStocksForDelisted error message is {}", throwable.getMessage());
				return Mono.empty();
			})
			.collect(Collectors.partitioningBy(
					KisSearchStockInfo::isDelisted,
					Collectors.mapping(KisSearchStockInfo::toEntity, Collectors.toList())
				)
			);
	}

	@NotNull
	private Set<String> findAllTickerSymbols() {
		return stockRepository.findAllStocks()
			.stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * 신규 배당 일정 저장
	 * 수행 과정
	 * - 배당 일정 조회
	 * - 배당 일정 저장
	 * @param tickerSymbols 배당 일정을 조회할 종목의 티커 심볼
	 * @return 저장된 배당 일정
	 */
	private List<StockDividend> reloadDividend(Set<String> tickerSymbols) {
		// 올해 배당 일정 조회
		int concurrency = 20;
		List<StockDividend> stockDividends = Flux.fromIterable(tickerSymbols)
			.flatMap(kisService::fetchDividend, concurrency)
			.delayElements(delayManager.delay())
			.collectList()
			.onErrorResume(throwable -> {
				log.error("reloadDividend error message is {}", throwable.getMessage());
				return Mono.empty();
			})
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyList).stream()
			.map(this::mapStockDividend)
			.toList();
		// 배당 일정 저장
		return dividendRepository.saveAll(stockDividends);
	}

	// 조회한 배당금을 엔티티 종목의 배당금으로 매핑
	private StockDividend mapStockDividend(KisDividend dividend) {
		return dividendRepository.findByTickerSymbolAndRecordDate(dividend.getTickerSymbol(), dividend.getRecordDate())
			.map(existStockDividend -> dividend.toEntity(existStockDividend.getId(), existStockDividend.getStock(),
				exDividendDateCalculator))
			.orElseGet(supplierNewStockDividend(dividend));
	}

	@NotNull
	private Supplier<StockDividend> supplierNewStockDividend(KisDividend dividend) {
		return () -> dividend.toEntity(findStockBy(dividend.getTickerSymbol()), exDividendDateCalculator);
	}

	private Stock findStockBy(String tickerSymbol) {
		return stockRepository.findByTickerSymbol(tickerSymbol)
			.orElseThrow(() -> new FineAntsException(StockErrorCode.NOT_FOUND_STOCK));
	}
}
