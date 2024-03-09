package codesquad.fineants.spring.api.kis.service;

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
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.HolidayManager;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import codesquad.fineants.spring.api.kis.response.LastDayClosingPriceResponse;
import codesquad.fineants.spring.api.stock_target_price.event.StockTargetPricePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class KisService {
	private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	private final KisClient kisClient;
	private final PortfolioHoldingRepository portFolioHoldingRepository;
	private final KisAccessTokenManager manager;
	private final CurrentPriceManager currentPriceManager;
	private final LastDayClosingPriceManager lastDayClosingPriceManager;
	private final HolidayManager holidayManager;
	private final StockTargetPricePublisher stockTargetPricePublisher;

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
	public List<CurrentPriceResponse> refreshAllStockCurrentPrice() {
		List<String> tickerSymbols = portFolioHoldingRepository.findAllTickerSymbol();
		List<CurrentPriceResponse> responses = refreshStockCurrentPrice(tickerSymbols);
		stockTargetPricePublisher.publishEvent(tickerSymbols);
		return responses;
	}

	// 주식 현재가 갱신
	public List<CurrentPriceResponse> refreshStockCurrentPrice(List<String> tickerSymbols) {
		List<CompletableFuture<CurrentPriceResponse>> futures = tickerSymbols.parallelStream()
			.map(this::createCompletableFuture)
			.collect(Collectors.toList());

		List<CurrentPriceResponse> currentPrices = futures.parallelStream()
			.map(this::getCurrentPriceResponseWithTimeout)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		currentPrices.forEach(currentPriceManager::addCurrentPrice);
		log.info("종목 현재가 {}개중 {}개 갱신", tickerSymbols.size(), currentPrices.size());
		return currentPrices;
	}

	private CompletableFuture<CurrentPriceResponse> createCompletableFuture(String tickerSymbol) {
		CompletableFuture<CurrentPriceResponse> future = new CompletableFuture<>();
		future.completeOnTimeout(null, 10, TimeUnit.SECONDS);
		future.exceptionally(e -> {
			log.error(e.getMessage());
			return null;
		});
		executorService.schedule(() -> {
			try {
				future.complete(fetchCurrentPrice(tickerSymbol));
			} catch (KisException e) {
				log.error(e.getMessage());
				future.completeExceptionally(e);
			}
		}, 1L, TimeUnit.SECONDS);
		return future;
	}

	public CurrentPriceResponse fetchCurrentPrice(String tickerSymbol) {
		Long currentPrice = kisClient.fetchCurrentPrice(tickerSymbol, manager.createAuthorization());
		return CurrentPriceResponse.create(tickerSymbol, currentPrice);
	}

	private CurrentPriceResponse getCurrentPriceResponseWithTimeout(CompletableFuture<CurrentPriceResponse> future) {
		try {
			return future.get(10L, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return null;
		}
	}

	private List<LastDayClosingPriceResponse> readLastDayClosingPriceResponses(List<String> unknownTickerSymbols) {
		List<CompletableFuture<LastDayClosingPriceResponse>> futures = unknownTickerSymbols.parallelStream()
			.map(this::createLastDayClosingPriceResponseCompletableFuture)
			.collect(Collectors.toList());
		return futures.parallelStream()
			.map(this::getLastDayClosingPriceResponseWithTimeout)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
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

	public List<LastDayClosingPriceResponse> refreshAllLastDayClosingPrice() {
		List<String> tickerSymbols = portFolioHoldingRepository.findAllTickerSymbol();
		return refreshLastDayClosingPrice(tickerSymbols);
	}

	public List<LastDayClosingPriceResponse> refreshLastDayClosingPrice(List<String> tickerSymbols) {
		List<LastDayClosingPriceResponse> lastDayClosingPrices = readLastDayClosingPriceResponses(tickerSymbols);
		lastDayClosingPrices.forEach(
			response -> lastDayClosingPriceManager.addPrice(response.getTickerSymbol(), response.getClosingPrice()));
		log.info("종목 종가 {}개중 {}개 갱신", tickerSymbols.size(), lastDayClosingPrices.size());
		return lastDayClosingPrices;
	}

	private LastDayClosingPriceResponse getLastDayClosingPriceResponseWithTimeout(
		CompletableFuture<LastDayClosingPriceResponse> future) {
		try {
			return future.get(10L, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return null;
		}
	}

	private CompletableFuture<LastDayClosingPriceResponse> createLastDayClosingPriceResponseCompletableFuture(
		String tickerSymbol) {
		CompletableFuture<LastDayClosingPriceResponse> future = new CompletableFuture<>();
		future.completeOnTimeout(null, 10L, TimeUnit.SECONDS);
		future.exceptionally(e -> {
			log.error(e.getMessage(), e);
			return null;
		});
		executorService.schedule(createLastDayClosingPriceRequest(tickerSymbol, future), 1L, TimeUnit.SECONDS);
		return future;
	}

	private Runnable createLastDayClosingPriceRequest(final String tickerSymbol,
		CompletableFuture<LastDayClosingPriceResponse> future) {
		return () -> {
			try {
				future.complete(kisClient.readLastDayClosingPrice(tickerSymbol, manager.createAuthorization()));
			} catch (KisException e) {
				future.completeExceptionally(e);
			}
		};
	}
}
