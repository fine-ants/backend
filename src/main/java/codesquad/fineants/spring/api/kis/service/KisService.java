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

	// 종목 가격정보 갱신
	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
	public void refreshStockPrice() {
		List<String> tickerSymbols = portFolioHoldingRepository.findAllTickerSymbol();
		refreshStockCurrentPrice(tickerSymbols);
		refreshLastDayClosingPrice(tickerSymbols);
	}

	// 주식 현재가 갱신
	private void refreshStockCurrentPrice(List<String> tickerSymbols) {
		List<CompletableFuture<CurrentPriceResponse>> futures = tickerSymbols.parallelStream()
			.map(this::createCompletableFuture)
			.collect(Collectors.toList());

		futures.parallelStream()
			.map(this::getCurrentPriceResponseWithTimeout)
			.filter(Objects::nonNull)
			.forEach(currentPriceManager::addCurrentPrice);
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
		List<CompletableFuture<LastDayClosingPriceResponse>> futures = tickerSymbols.parallelStream()
			.filter(tickerSymbol -> !lastDayClosingPriceManager.hasPrice(tickerSymbol))
			.map(this::createLastDayClosingPriceResponseCompletableFuture)
			.collect(Collectors.toList());
		futures.parallelStream()
			.map(this::getLastDayClosingPriceResponseWithTimeout)
			.peek(response -> log.info("종가 갱신 응답 : {}", response))
			.filter(Objects::nonNull)
			.forEach(response -> lastDayClosingPriceManager.addPrice(response.getTickerSymbol(), response.getPrice()));
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
