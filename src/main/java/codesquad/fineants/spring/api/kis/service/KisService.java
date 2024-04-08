package codesquad.fineants.spring.api.kis.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.spring.api.common.errors.exception.KisException;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.client.KisCurrentPrice;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.HolidayManager;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.kis.response.KisClosingPrice;
import codesquad.fineants.spring.api.notification.event.PortfolioPublisher;
import codesquad.fineants.spring.api.stock_target_price.event.StockTargetPricePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class KisService {
	private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	public static final Duration TIMEOUT = Duration.ofMinutes(1L);

	private final KisClient kisClient;
	private final PortfolioHoldingRepository portFolioHoldingRepository;
	private final KisAccessTokenManager manager;
	private final CurrentPriceManager currentPriceManager;
	private final LastDayClosingPriceManager lastDayClosingPriceManager;
	private final HolidayManager holidayManager;
	private final StockTargetPricePublisher stockTargetPricePublisher;
	private final PortfolioPublisher portfolioPublisher;

	// 평일 9am ~ 15:59pm 5초마다 현재가 갱신 수행
	@Scheduled(cron = "0/5 * 9-15 ? * MON,TUE,WED,THU,FRI")
	public void scheduleRefreshingAllStockCurrentPrice() {
		// 휴장일인 경우 실행하지 않음
		if (holidayManager.isHoliday(LocalDate.now())) {
			return;
		}
		refreshAllStockCurrentPrice();
	}

	// 회원이 가지고 있는 모든 종목에 대하여 현재가 갱신
	public List<KisCurrentPrice> refreshAllStockCurrentPrice() {
		List<String> tickerSymbols = portFolioHoldingRepository.findAllTickerSymbol();
		List<KisCurrentPrice> prices = refreshStockCurrentPrice(tickerSymbols);
		stockTargetPricePublisher.publishEvent(tickerSymbols);
		portfolioPublisher.publishCurrentPriceEvent();
		return prices;
	}

	// 주식 현재가 갱신
	public List<KisCurrentPrice> refreshStockCurrentPrice(List<String> tickerSymbols) {
		List<CompletableFuture<KisCurrentPrice>> futures = tickerSymbols.stream()
			.map(this::submitCurrentPriceFuture)
			.collect(Collectors.toList());

		List<KisCurrentPrice> prices = futures.stream()
			.map(future -> {
				try {
					return future.get(1L, TimeUnit.MINUTES);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		prices.forEach(currentPriceManager::addCurrentPrice);

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

	public Mono<KisCurrentPrice> fetchCurrentPrice(String tickerSymbol) {
		return kisClient.fetchCurrentPrice(tickerSymbol, manager.createAuthorization());
	}

	// 15시 30분에 종가 갱신 수행
	@Scheduled(cron = "* 30 15 * * *")
	public void scheduleRefreshingAllLastDayClosingPrice() {
		// 휴장일인 경우 실행하지 않음
		if (holidayManager.isHoliday(LocalDate.now())) {
			return;
		}
		refreshAllLastDayClosingPrice();
	}

	// 종목 종가 모두 갱신
	public List<KisClosingPrice> refreshAllLastDayClosingPrice() {
		List<String> tickerSymbols = portFolioHoldingRepository.findAllTickerSymbol();
		return refreshLastDayClosingPrice(tickerSymbols);
	}

	// 종목 종가 일부 갱신
	public List<KisClosingPrice> refreshLastDayClosingPrice(List<String> tickerSymbols) {
		List<KisClosingPrice> lastDayClosingPrices = readLastDayClosingPriceResponses(tickerSymbols);
		lastDayClosingPrices.forEach(lastDayClosingPriceManager::addPrice);
		log.info("종목 종가 {}개중 {}개 갱신", tickerSymbols.size(), lastDayClosingPrices.size());
		return lastDayClosingPrices;
	}

	private List<KisClosingPrice> readLastDayClosingPriceResponses(List<String> unknownTickerSymbols) {
		List<CompletableFuture<KisClosingPrice>> futures = unknownTickerSymbols.stream()
			.map(this::createClosingPriceFuture)
			.collect(Collectors.toList());

		return futures.stream()
			.map(future -> {
				try {
					return future.get(1L, TimeUnit.MINUTES);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
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

	public Mono<KisClosingPrice> fetchClosingPrice(String tickerSymbol) {
		return kisClient.fetchClosingPrice(tickerSymbol, manager.createAuthorization());
	}
}
