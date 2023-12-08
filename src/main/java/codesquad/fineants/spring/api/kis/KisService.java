package codesquad.fineants.spring.api.kis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.kis.manager.PortfolioSubscriptionManager;
import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import codesquad.fineants.spring.api.kis.response.LastDayClosingPriceResponse;
import codesquad.fineants.spring.api.portfolio_stock.PortfolioStockService;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class KisService {
	private static final String SUBSCRIBE_PORTFOLIO_HOLDING_FORMAT = "/sub/portfolio/%d";
	private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

	private final KisClient kisClient;
	private final PortfolioHoldingRepository portFolioHoldingRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final PortfolioStockService portfolioStockService;
	private final KisAccessTokenManager manager;
	private final CurrentPriceManager currentPriceManager;
	private final PortfolioSubscriptionManager portfolioSubscriptionManager;
	private final LastDayClosingPriceManager lastDayClosingPriceManager;
	private final Executor portfolioDetailExecutor = Executors.newFixedThreadPool(100, r -> {
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		return thread;
	});

	public void addPortfolioSubscription(String sessionId, PortfolioSubscription subscription) {
		subscription.getTickerSymbols().stream()
			.filter(tickerSymbol -> !currentPriceManager.hasKey(tickerSymbol))
			.forEach(currentPriceManager::addKey);
		portfolioSubscriptionManager.addPortfolioSubscription(sessionId, subscription);
	}

	public void removePortfolioSubscription(String sessionId) {
		portfolioSubscriptionManager.removePortfolioSubscription(sessionId);
	}

	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
	protected void publishPortfolioDetail() {
		List<CompletableFuture<PortfolioHoldingsResponse>> futures = portfolioSubscriptionManager.values()
			.parallelStream()
			.filter(this::hasAllCurrentPrice)
			.map(PortfolioSubscription::getPortfolioId)
			.map(portfolioId -> CompletableFuture.supplyAsync(
					() -> portfolioStockService.readMyPortfolioStocks(portfolioId),
					portfolioDetailExecutor)
				.exceptionally(e -> {
					log.info(e.getMessage(), e);
					return null;
				}))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		futures.parallelStream()
			.map(CompletableFuture::join)
			.forEach(response -> messagingTemplate.convertAndSend(
				String.format(SUBSCRIBE_PORTFOLIO_HOLDING_FORMAT, response.getPortfolioId()), response));
	}

	public CompletableFuture<PortfolioHoldingsResponse> publishPortfolioDetail(Long portfolioId) {
		return CompletableFuture.supplyAsync(() ->
			portfolioSubscriptionManager.getPortfolioSubscription(portfolioId)
				.map(PortfolioSubscription::getPortfolioId)
				.map(portfolioStockService::readMyPortfolioStocks)
				.orElse(null));
	}

	private boolean hasAllCurrentPrice(PortfolioSubscription subscription) {
		return subscription.getTickerSymbols().stream()
			.allMatch(currentPriceManager::hasCurrentPrice);
	}

	// 종목 가격정보 갱신
	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
	public void refreshStockPrice() {
		List<String> tickerSymbols = portFolioHoldingRepository.findAllTickerSymbol();
		refreshStockCurrentPrice(tickerSymbols);
		refreshLastDayClosingPrice(tickerSymbols);
	}

	// 주식 현재가 갱신
	public void refreshStockCurrentPrice(List<String> tickerSymbols) {
		List<CompletableFuture<CurrentPriceResponse>> futures = tickerSymbols.parallelStream()
			.map(tickerSymbol -> {
				CompletableFuture<CurrentPriceResponse> future = new CompletableFuture<>();
				executorService.schedule(createCurrentPriceRequest(tickerSymbol, future), 200, TimeUnit.MILLISECONDS);
				return future;
			}).collect(Collectors.toList());

		futures.parallelStream()
			.map(CompletableFuture::join)
			.filter(Objects::nonNull)
			.forEach(currentPriceManager::addCurrentPrice);
	}

	private Runnable createCurrentPriceRequest(final String tickerSymbol,
		CompletableFuture<CurrentPriceResponse> future) {
		return () -> {
			CurrentPriceResponse response = readRealTimeCurrentPrice(tickerSymbol);
			future.completeOnTimeout(response, 10, TimeUnit.SECONDS);
			future.exceptionally(e -> {
				log.info(e.getMessage(), e);
				return null;
			});
		};
	}

	// 제약조건 : kis 서버에 1초당 최대 5건, TR간격 0.1초 이하면 안됨
	public CurrentPriceResponse readRealTimeCurrentPrice(String tickerSymbol) {
		long currentPrice = kisClient.readRealTimeCurrentPrice(tickerSymbol, manager.createAuthorization());
		log.info("tickerSymbol={}, currentPrice={}, time={}", tickerSymbol, currentPrice, LocalDateTime.now());
		return new CurrentPriceResponse(tickerSymbol, currentPrice);
	}

	// 종가 갱신 (매일 0시 1분 0초에 시작)
	public void refreshLastDayClosingPrice(List<String> tickerSymbols) {
		List<CompletableFuture<LastDayClosingPriceResponse>> futures = tickerSymbols.parallelStream()
			.filter(tickerSymbol -> !lastDayClosingPriceManager.hasPrice(tickerSymbol))
			.map(tickerSymbol -> {
				CompletableFuture<LastDayClosingPriceResponse> future = new CompletableFuture<>();
				executorService.schedule(createLastDayClosingPriceRequest(tickerSymbol, future), 200L,
					TimeUnit.MILLISECONDS);
				return future;
			})
			.collect(Collectors.toList());
		futures.parallelStream()
			.map(CompletableFuture::join)
			.peek(response -> log.info("종가 갱신 응답 : {}", response))
			.filter(Objects::nonNull)
			.forEach(response -> lastDayClosingPriceManager.addPrice(response.getTickerSymbol(), response.getPrice()));
	}

	private Runnable createLastDayClosingPriceRequest(final String tickerSymbol,
		CompletableFuture<LastDayClosingPriceResponse> future) {
		return () -> {
			try {
				future.completeOnTimeout(kisClient.readLastDayClosingPrice(tickerSymbol, manager.createAuthorization()),
					10, TimeUnit.SECONDS);
			} catch (Exception e) {
				future.completeExceptionally(e);
			}
			future.exceptionally(e -> {
				log.error(e.getMessage(), e);
				return null;
			});
		};
	}
}
