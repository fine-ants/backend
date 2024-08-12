package codesquad.fineants.domain.kis.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.kis.aop.CheckedKisAccessToken;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividend;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividendWrapper;
import codesquad.fineants.domain.kis.domain.dto.response.KisIpo;
import codesquad.fineants.domain.kis.domain.dto.response.KisSearchStockInfo;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.kis.repository.HolidayRepository;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.domain.notification.event.publisher.PortfolioPublisher;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock_target_price.domain.entity.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.event.publisher.StockTargetPricePublisher;
import codesquad.fineants.domain.stock_target_price.repository.StockTargetPriceRepository;
import codesquad.fineants.global.errors.exception.KisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@RequiredArgsConstructor
@Service
public class KisService {
	private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	public static final Duration DELAY = Duration.ofMillis(50L);
	public static final Duration TIMEOUT = Duration.ofMinutes(10L);

	private final KisClient kisClient;
	private final PortfolioHoldingRepository portFolioHoldingRepository;
	private final KisAccessTokenRepository manager;
	private final CurrentPriceRepository currentPriceRepository;
	private final ClosingPriceRepository closingPriceRepository;
	private final HolidayRepository holidayRepository;
	private final StockTargetPricePublisher stockTargetPricePublisher;
	private final PortfolioPublisher portfolioPublisher;
	private final StockTargetPriceRepository stockTargetPriceRepository;

	// 평일 9am ~ 15:59pm 5초마다 현재가 갱신 수행
	@Profile(value = "production")
	@Scheduled(cron = "0/5 * 9-15 ? * MON,TUE,WED,THU,FRI")
	@CheckedKisAccessToken
	@Transactional(readOnly = true)
	public void refreshCurrentPrice() {
		// 휴장일인 경우 실행하지 않음
		if (holidayRepository.isHoliday(LocalDate.now())) {
			return;
		}
		refreshAllStockCurrentPrice();
	}

	// 회원이 가지고 있는 모든 종목에 대하여 현재가 갱신
	@CheckedKisAccessToken
	@Transactional(readOnly = true)
	public List<KisCurrentPrice> refreshAllStockCurrentPrice() {
		Set<String> totalTickerSymbol = new HashSet<>();
		totalTickerSymbol.addAll(portFolioHoldingRepository.findAllTickerSymbol());
		totalTickerSymbol.addAll(stockTargetPriceRepository.findAll().stream()
			.map(StockTargetPrice::getStock)
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toSet()));
		List<String> totalTickerSymbolList = totalTickerSymbol.stream().toList();

		List<KisCurrentPrice> prices = this.refreshStockCurrentPrice(totalTickerSymbolList);
		stockTargetPricePublisher.publishEvent(totalTickerSymbolList);
		portfolioPublisher.publishCurrentPriceEvent();
		return prices;
	}

	// 주식 현재가 갱신
	@CheckedKisAccessToken
	@Transactional(readOnly = true)
	public List<KisCurrentPrice> refreshStockCurrentPrice(List<String> tickerSymbols) {
		List<CompletableFuture<KisCurrentPrice>> futures = tickerSymbols.stream()
			.map(this::submitCurrentPriceFuture)
			.toList();

		List<KisCurrentPrice> prices = futures.stream()
			.map(future -> {
				try {
					return future.get(1L, TimeUnit.MINUTES);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					log.error(e.getMessage());
					return null;
				}
			})
			.filter(Objects::nonNull)
			.toList();

		prices.forEach(currentPriceRepository::addCurrentPrice);

		log.info("종목 현재가 {}개중 {}개 갱신", tickerSymbols.size(), prices.size());
		return prices;
	}

	private CompletableFuture<KisCurrentPrice> submitCurrentPriceFuture(String tickerSymbol) {
		CompletableFuture<KisCurrentPrice> future = createCompletableFuture();
		executorService.schedule(createCurrentPriceRunnable(tickerSymbol, future), 1L, TimeUnit.SECONDS);
		return future;
	}

	private <T> CompletableFuture<T> createCompletableFuture() {
		CompletableFuture<T> future = new CompletableFuture<>();
		future.orTimeout(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
		future.exceptionally(e -> {
			log.error(e.getMessage());
			return null;
		});
		return future;
	}

	private Runnable createCurrentPriceRunnable(String tickerSymbol, CompletableFuture<KisCurrentPrice> future) {
		return () -> {
			try {
				future.complete(fetchCurrentPrice(tickerSymbol)
					.blockOptional(Duration.ofMinutes(1L))
					.orElseGet(() -> KisCurrentPrice.empty(tickerSymbol))
				);
			} catch (KisException e) {
				future.completeExceptionally(e);
			}
		};
	}

	@CheckedKisAccessToken
	public Mono<KisCurrentPrice> fetchCurrentPrice(String tickerSymbol) {
		return kisClient.fetchCurrentPrice(tickerSymbol);
	}

	// 15시 30분에 종가 갱신 수행
	@Scheduled(cron = "* 30 15 * * *")
	@Transactional(readOnly = true)
	@CheckedKisAccessToken
	public void refreshClosingPrice() {
		// 휴장일인 경우 실행하지 않음
		if (holidayRepository.isHoliday(LocalDate.now())) {
			return;
		}
		refreshAllLastDayClosingPrice();
	}

	// 종목 종가 모두 갱신
	@CheckedKisAccessToken
	public List<KisClosingPrice> refreshAllLastDayClosingPrice() {
		List<String> tickerSymbols = portFolioHoldingRepository.findAllTickerSymbol();
		return refreshLastDayClosingPrice(tickerSymbols);
	}

	// 종목 종가 일부 갱신
	@CheckedKisAccessToken
	public List<KisClosingPrice> refreshLastDayClosingPrice(List<String> tickerSymbols) {
		List<KisClosingPrice> lastDayClosingPrices = readLastDayClosingPriceResponses(tickerSymbols);
		lastDayClosingPrices.forEach(closingPriceRepository::addPrice);
		log.info("종목 종가 {}개중 {}개 갱신", tickerSymbols.size(), lastDayClosingPrices.size());
		return lastDayClosingPrices;
	}

	private List<KisClosingPrice> readLastDayClosingPriceResponses(List<String> unknownTickerSymbols) {
		List<CompletableFuture<KisClosingPrice>> futures = unknownTickerSymbols.stream()
			.map(this::createClosingPriceFuture)
			.toList();

		return futures.stream()
			.map(future -> {
				try {
					return future.get(1L, TimeUnit.MINUTES);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.toList();
	}

	private CompletableFuture<KisClosingPrice> createClosingPriceFuture(
		String tickerSymbol) {
		CompletableFuture<KisClosingPrice> future = createCompletableFuture();
		executorService.schedule(() -> {
			try {
				future.complete(fetchClosingPrice(tickerSymbol)
					.blockOptional(TIMEOUT)
					.orElseGet(() -> KisClosingPrice.empty(tickerSymbol)));
			} catch (KisException e) {
				future.completeExceptionally(e);
			}
		}, 1L, TimeUnit.SECONDS);
		return future;
	}

	@CheckedKisAccessToken
	public Mono<KisClosingPrice> fetchClosingPrice(String tickerSymbol) {
		return kisClient.fetchClosingPrice(tickerSymbol);
	}

	/**
	 * tickerSymbol에 해당하는 종목의 배당 일정을 조회합니다.
	 * @param tickerSymbol 종목 단축 코드
	 * @return 종목의 배당 일정 정보
	 */
	@CheckedKisAccessToken
	public Mono<List<KisDividend>> fetchDividend(String tickerSymbol) {
		return kisClient.fetchDividendThisYear(tickerSymbol)
			.map(KisDividendWrapper::getKisDividends)
			.doOnSuccess(response -> log.debug("fetchDividend list is {}", response.size()))
			.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)))
			.onErrorResume(e -> Mono.empty());
	}

	@CheckedKisAccessToken
	public List<KisDividend> fetchDividendAll(LocalDate from, LocalDate to) {
		return kisClient.fetchDividendAll(from, to)
			.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)))
			.blockOptional(TIMEOUT)
			.orElseGet(Collections::emptyList).stream()
			.sorted()
			.toList();
	}

	/**
	 * 종목 기본 조회
	 * @param tickerSymbol 종목 티커 심볼
	 * @return 종목 정보
	 */
	@CheckedKisAccessToken
	public Mono<KisSearchStockInfo> fetchSearchStockInfo(String tickerSymbol) {
		return kisClient.fetchSearchStockInfo(tickerSymbol)
			.doOnSuccess(response -> log.debug("fetchSearchStockInfo ticker is {}", response.getTickerSymbol()))
			.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)))
			.onErrorResume(e -> Mono.empty());
	}

	/**
	 * 상장된 종목 조회
	 * 하루전부터 오늘까지의 상장된 종목들의 정보를 조회한다.
	 * @return 종목 정보 리스트
	 */
	@CheckedKisAccessToken
	public Set<StockDataResponse.StockIntegrationInfo> fetchStockInfoInRangedIpo() {
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		Set<String> tickerSymbols = kisClient.fetchIpo(yesterday, today)
			.blockOptional(TIMEOUT)
			.orElseThrow()
			.getKisIpos().stream()
			.filter(kisIpo -> !kisIpo.isEmpty())
			.map(KisIpo::getShtCd)
			.collect(Collectors.toSet());

		int concurrency = 20;
		return Flux.fromIterable(tickerSymbols)
			.flatMap(this::fetchSearchStockInfo, concurrency)
			.delayElements(DELAY)
			.collectList()
			.blockOptional(TIMEOUT)
			.orElseGet(Collections::emptyList).stream()
			.map(KisSearchStockInfo::toEntity)
			.map(StockDataResponse.StockIntegrationInfo::from)
			.collect(Collectors.toUnmodifiableSet());
	}
}
