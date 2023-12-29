package codesquad.fineants.spring.api.kis.service;

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
import codesquad.fineants.spring.api.errors.exception.KisException;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import codesquad.fineants.spring.api.kis.response.LastDayClosingPriceResponse;
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

	@Scheduled(fixedRate = 5L, timeUnit = TimeUnit.SECONDS)
	public void refreshStockPrice() {
		List<String> tickerSymbols = portFolioHoldingRepository.findAllTickerSymbol();
		refreshStockCurrentPrice(tickerSymbols);
		refreshLastDayClosingPrice(tickerSymbols);
	}

	// 주식 현재가 갱신
	public void refreshStockCurrentPrice(List<String> tickerSymbols) {
		List<CompletableFuture<CurrentPriceResponse>> futures = tickerSymbols.parallelStream()
			.map(this::createCompletableFuture)
			.collect(Collectors.toList());

		List<CurrentPriceResponse> currentPrices = futures.parallelStream()
			.map(this::getCurrentPriceResponseWithTimeout)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		currentPrices.forEach(currentPriceManager::addCurrentPrice);

		// 갱신 실패한 종목에 대해서 성공할때까지 갱신을 시도
		int refreshedCount = currentPrices.size();
		if (refreshedCount != tickerSymbols.size()) {
			List<String> refreshedTickerSymbols = currentPrices.stream()
				.map(CurrentPriceResponse::getTickerSymbol)
				.collect(Collectors.toList());
			List<String> failTickerSymbols = tickerSymbols.stream()
				.filter(tickerSymbol -> !refreshedTickerSymbols.contains(tickerSymbol))
				.collect(Collectors.toList());
			refreshStockCurrentPrice(failTickerSymbols);
		}

		log.info("종목 현재가 {}개중 {}개 갱신", tickerSymbols.size(), refreshedCount);
	}

	private CompletableFuture<CurrentPriceResponse> createCompletableFuture(String tickerSymbol) {
		CompletableFuture<CurrentPriceResponse> future = new CompletableFuture<>();
		future.completeOnTimeout(null, 10, TimeUnit.SECONDS);
		future.exceptionally(e -> {
			log.error(e.getMessage(), e);
			return null;
		});
		executorService.schedule(createCurrentPriceRequest(tickerSymbol, future), 1L, TimeUnit.SECONDS);
		return future;
	}

	private Runnable createCurrentPriceRequest(final String tickerSymbol,
		CompletableFuture<CurrentPriceResponse> future) {
		return () -> {
			try {
				future.complete(readRealTimeCurrentPrice(tickerSymbol));
			} catch (KisException e) {
				future.completeExceptionally(e);
			}
		};
	}

	public CurrentPriceResponse readRealTimeCurrentPrice(String tickerSymbol) {
		long currentPrice = kisClient.readRealTimeCurrentPrice(tickerSymbol, manager.createAuthorization());
		return new CurrentPriceResponse(tickerSymbol, currentPrice);
	}

	private CurrentPriceResponse getCurrentPriceResponseWithTimeout(CompletableFuture<CurrentPriceResponse> future) {
		try {
			return future.get(10L, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return null;
		}
	}

	public void refreshLastDayClosingPrice(List<String> tickerSymbols) {
		List<String> unknownTickerSymbols = tickerSymbols.stream()
			.filter(tickerSymbol -> !lastDayClosingPriceManager.hasPrice(tickerSymbol))
			.collect(Collectors.toList());

		List<CompletableFuture<LastDayClosingPriceResponse>> futures = unknownTickerSymbols.parallelStream()
			.map(this::createLastDayClosingPriceResponseCompletableFuture)
			.collect(Collectors.toList());
		List<LastDayClosingPriceResponse> lastDayClosingPrices = futures.parallelStream()
			.map(this::getLastDayClosingPriceResponseWithTimeout)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		lastDayClosingPrices.forEach(
			response -> lastDayClosingPriceManager.addPrice(response.getTickerSymbol(), response.getPrice()));

		// 종가 갱신 실패하 종목들을 성공할때가지 계속 시도합니다.
		int count = lastDayClosingPrices.size();
		if (count != unknownTickerSymbols.size()) {
			List<String> refreshedTickerSymbols = lastDayClosingPrices.stream()
				.map(LastDayClosingPriceResponse::getTickerSymbol)
				.collect(Collectors.toList());
			List<String> failTickerSymbols = unknownTickerSymbols.stream()
				.filter(tickerSymbol -> !refreshedTickerSymbols.contains(tickerSymbol))
				.collect(Collectors.toList());
			refreshLastDayClosingPrice(failTickerSymbols);
		}

		log.info("종목 종가 {}개중 {}개 갱신", unknownTickerSymbols.size(), count);
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
