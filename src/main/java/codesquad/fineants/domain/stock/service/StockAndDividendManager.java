package codesquad.fineants.domain.stock.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.dividend.repository.StockDividendRepository;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividend;
import codesquad.fineants.domain.kis.domain.dto.response.KisSearchStockInfo;
import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockRefreshResponse;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.global.errors.errorcode.StockErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockAndDividendManager {

	private static final Duration DELAY = Duration.ofMillis(50);
	private static final Duration TIMEOUT = Duration.ofMinutes(10);

	private final StockRepository stockRepository;
	private final StockDividendRepository dividendRepository;
	private final KisService kisService;

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
	public StockRefreshResponse reloadStocks() {
		// 신규 상장 종목 저장
		Set<String> newlyAddedTickerSymbols = saveIpoStock();

		// 상장 폐지 종목 조회
		Map<Boolean, List<KisSearchStockInfo>> delistedPartitionStockMap = fetchDelistedStocks();

		// 상장 폐지 종목 및 종목의 배당 일정 삭제
		Set<String> deletedTickerSymbols = delistedPartitionStockMap.get(true).stream()
			.map(KisSearchStockInfo::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
		deleteStocks(deletedTickerSymbols);

		// 올해 신규 배당 일정 저장
		Set<String> listedTickerSymbols = delistedPartitionStockMap.get(false).stream()
			.map(KisSearchStockInfo::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
		List<StockDividend> savedStockDividends = reloadDividend(listedTickerSymbols);
		log.info("reloadStocks savedStockDividends={}", savedStockDividends.size());

		return StockRefreshResponse.create(newlyAddedTickerSymbols, deletedTickerSymbols);
	}

	/**
	 * 종목 삭제
	 * 종목 삭제시 종목에 해당하는 배당 일정을 사전에 제거한다
	 * @param tickerSymbols 삭제할 종목의 티커 심볼
	 */
	private void deleteStocks(Set<String> tickerSymbols) {
		// 종목의 배당금 삭제
		int deleteCount = dividendRepository.deleteByTickerSymbols(tickerSymbols);
		log.info("delete dividends for TickerSymbols : {}, deleteCount={}", tickerSymbols, deleteCount);

		// 종목 삭제
		deleteCount = stockRepository.deleteAllByTickerSymbols(tickerSymbols);
		log.info("delete stocks for TickerSymbols : {}, deleteCount={}", tickerSymbols, deleteCount);
	}

	/**
	 * 신규 상장 종목 저장
	 * @return 신규 상장 종목들의 티커 심볼 집합
	 */
	@NotNull
	private Set<String> saveIpoStock() {
		// 신규 상장 종목 조회
		List<Stock> stocks = kisService.fetchStockInfoInRangedIpo().stream()
			.map(StockDataResponse.StockIntegrationInfo::toEntity)
			.toList();
		stocks.forEach(stock -> log.info("ipo Stock : {}", stock));

		// 상장된 종목 저장
		return stockRepository.saveAll(stocks).stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * 상장 폐지 종목 조회
	 * @return 상장 폐지 종목 분할 맵
	 */
	private Map<Boolean, List<KisSearchStockInfo>> fetchDelistedStocks() {
		// 종목 전체 조회
		Set<String> tickerSymbols = stockRepository.findAll()
			.stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());

		// 한국 투자 증권에 종목 정보 조회
		int concurrency = 20;
		Flux<KisSearchStockInfo> flux = Flux.fromIterable(tickerSymbols)
			.flatMap(kisService::fetchSearchStockInfo, concurrency)
			.delayElements(DELAY);

		// 상장 폐지 종목 분할
		return flux.collectList()
			.blockOptional(TIMEOUT)
			.map(list -> list.stream().collect(Collectors.partitioningBy(KisSearchStockInfo::isDelisted)))
			.orElseGet(() -> Map.of(true, Collections.emptyList(), false, Collections.emptyList()));
	}

	/**
	 * 신규 배당 일정 저장
	 * @param tickerSymbols 배당 일정을 조회할 종목의 티커 심볼
	 * @return 저장된 배당 일정
	 */
	private List<StockDividend> reloadDividend(Set<String> tickerSymbols) {
		// 올해 배당 일정 조회
		int concurrency = 20;
		List<KisDividend> dividends = Flux.fromIterable(tickerSymbols)
			.flatMap(ticker -> kisService.fetchDividend(ticker).flatMapMany(Flux::fromIterable), concurrency)
			.delayElements(DELAY)
			.collectList()
			.blockOptional(TIMEOUT)
			.orElseGet(Collections::emptyList);

		// 배당 일정 저장
		List<StockDividend> stockDividends = dividends.stream()
			.map(dividend -> {
				StockDividend existStockDividend = dividendRepository.findByTickerSymbolAndRecordDate(
					dividend.getTickerSymbol(), dividend.getRecordDate()).orElse(null);
				if (existStockDividend == null) {
					Stock stock = stockRepository.findByTickerSymbol(dividend.getTickerSymbol())
						.orElseThrow(() -> new FineAntsException(StockErrorCode.NOT_FOUND_STOCK));
					return dividend.toEntity(stock);
				}
				return dividend.toEntity(existStockDividend.getId(), existStockDividend.getStock());
			}).toList();
		return dividendRepository.saveAll(stockDividends);
	}
}
