package codesquad.fineants.domain.stock.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.dividend.repository.StockDividendRepository;
import codesquad.fineants.domain.dividend.service.StockDividendService;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividend;
import codesquad.fineants.domain.kis.domain.dto.response.KisSearchStockInfo;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.stock.domain.dto.request.StockSearchRequest;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockRefreshResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockSearchItem;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockQueryRepository;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.global.errors.errorcode.StockErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockService {
	private final StockRepository stockRepository;
	private final StockDividendRepository dividendRepository;
	private final CurrentPriceRepository currentPriceRepository;
	private final ClosingPriceRepository closingPriceRepository;
	private final StockDividendService stockDividendService;
	private final StockQueryRepository stockQueryRepository;
	private final KisService kisService;

	@Transactional(readOnly = true)
	public List<StockSearchItem> search(StockSearchRequest request) {
		return stockRepository.search(request.getSearchTerm())
			.stream()
			.map(StockSearchItem::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<StockSearchItem> search(String tickerSymbol, int size, String keyword) {
		return stockQueryRepository.getSliceOfStock(tickerSymbol, size, keyword).stream()
			.map(StockSearchItem::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public StockResponse getStock(String tickerSymbol) {
		Stock stock = stockRepository.findByTickerSymbol(tickerSymbol)
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK));
		return StockResponse.of(stock, currentPriceRepository, closingPriceRepository);
	}

	@Scheduled(cron = "0 0 8 * * ?") // 매일 오전 8시 (초, 분, 시간)
	@Transactional
	public void scheduledReloadStocks() {
		StockRefreshResponse response = this.reloadStocks();
		log.info("refreshStocks response : {}", response);
		stockDividendService.writeDividendCsvToS3();
	}

	/**
	 * 종목 및 배당 일정을 최신화
	 * - 새로 상장된 종목 추가
	 * - 상장 폐지된 종목 삭제
	 * - 이번 년도 종목의 배당 일정 조회 후 저장
	 * @return 상장 종목 및 폐지 종목 응답
	 */
	@Transactional
	public StockRefreshResponse reloadStocks() {
		// 신규 상장 종목 조회
		List<Stock> stocks = kisService.fetchStockInfoInRangedIpo().stream()
			.map(StockDataResponse.StockIntegrationInfo::toEntity)
			.toList();
		stocks.forEach(stock -> log.info("newlyAdded Stock : {}", stock));

		// 상장된 종목 저장
		Set<String> newlyAddedTickerSymbols = stockRepository.saveAll(stocks).stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
		newlyAddedTickerSymbols.forEach(ticker -> log.info("Save the item in the database and ticker : {}", ticker));

		// 1. 종목 전체 조회 및 티커 심볼 집합으로 변환
		Set<String> tickerSymbols = stockRepository.findAll()
			.stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());

		// 2. 모든 종목의 현재가를 조회하여 폐지된 종목이 있는지 조회
		Flux<KisSearchStockInfo> flux = Flux.fromIterable(tickerSymbols)
			.flatMap(tickerSymbol -> kisService.fetchSearchStockInfo(tickerSymbol)
				.doOnSuccess(response -> log.info("fetchSearchStockInfo ticker is {}", response.getTickerSymbol()))
				.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)))
				.onErrorResume(e -> Mono.empty()), 20)
			.delayElements(Duration.ofMillis(50));

		Map<Boolean, List<KisSearchStockInfo>> partitionedStocks = flux.collectList()
			.blockOptional(Duration.ofMinutes(10))
			.map(list -> list.stream().collect(Collectors.partitioningBy(KisSearchStockInfo::isDelisted)))
			.orElseGet(() -> Map.of(true, Collections.emptyList(), false, Collections.emptyList()));
		List<KisSearchStockInfo> delistedStocks = partitionedStocks.get(true); // 상장 폐지 종목
		List<KisSearchStockInfo> currentStocks = partitionedStocks.get(false); // 상장 종목

		// 3. 상장 폐지된 종목의 배당금 제거
		Set<String> deletedStockCode = delistedStocks.stream()
			.map(KisSearchStockInfo::getStockCode)
			.collect(Collectors.toUnmodifiableSet());
		dividendRepository.deleteByStockCodes(deletedStockCode);
		log.info("delete dividends for StockCodes : {}", deletedStockCode);

		// 4. 상장 폐지된 종목 소프트 제거
		int deleted = stockRepository.deleteAllByStockCodes(deletedStockCode);
		log.info("delete stocks for TickerSymbols : {}, deleted={}", deletedStockCode, deleted);

		// 5. 상장된 종목들을 대상으로 이번년도 배당 일정 조회
		List<KisDividend> dividends = Flux.fromIterable(currentStocks)
			.map(KisSearchStockInfo::getTickerSymbol)
			.flatMap(ticker -> kisService.fetchDividend(ticker)
				.doOnSuccess(response -> log.info("fetchDividend list is {}", response.size()))
				.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)))
				.onErrorResume(e -> Mono.empty())
				.flatMapMany(Flux::fromIterable), 20)
			.delayElements(Duration.ofMillis(50))
			.collectList()
			.blockOptional(Duration.ofMinutes(10))
			.orElseGet(Collections::emptyList);

		// 6. 배당 일정 저장
		List<StockDividend> stockDividends = dividends.stream()
			.map(dividend -> {
				Stock stock = stockRepository.findByTickerSymbol(dividend.getTickerSymbol())
					.orElseThrow(() -> new FineAntsException(StockErrorCode.NOT_FOUND_STOCK));
				return dividend.toEntity(stock);
			}).toList();
		stockDividends.forEach(stockDividend -> {
			try {
				dividendRepository.save(stockDividend);
			} catch (Exception e) {
				log.error("Error saving stock dividend: {}", stockDividend, e);
			}
		});
		return StockRefreshResponse.create(newlyAddedTickerSymbols, deletedStockCode);
	}
}
