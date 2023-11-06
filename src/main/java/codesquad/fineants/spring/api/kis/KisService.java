package codesquad.fineants.spring.api.kis;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.manager.PortfolioSubscriptionManager;
import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import codesquad.fineants.spring.api.portfolio_stock.PortfolioStockService;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class KisService {
	private static final String SUBSCRIBE_PORTFOLIO_HOLDING_FORMAT = "/sub/portfolio/%d";
	private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

	private final KisClient kisClient;
	private final PortfolioRepository portfolioRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final PortfolioStockService portfolioStockService;
	private final KisAccessTokenManager manager;
	private final CurrentPriceManager currentPriceManager;
	private final PortfolioSubscriptionManager portfolioSubscriptionManager;
	private final Executor portfolioDetailExecutor = Executors.newFixedThreadPool(100, r -> {
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		return thread;
	});

	// 제약조건 : kis 서버에 1초당 최대 5건, TR간격 0.1초 이하면 안됨
	public CurrentPriceResponse readRealTimeCurrentPrice(String tickerSymbol) {
		long currentPrice = kisClient.readRealTimeCurrentPrice(tickerSymbol, manager.createAuthorization());
		log.info("tickerSymbol={}, currentPrice={}, time={}", tickerSymbol, currentPrice, LocalDateTime.now());
		return new CurrentPriceResponse(tickerSymbol, currentPrice);
	}

	public void addPortfolioSubscription(String sessionId, PortfolioSubscription subscription) {
		addTickerSymbols(subscription.getTickerSymbols());
		portfolioSubscriptionManager.addPortfolioSubscription(sessionId, subscription);
	}

	public void addTickerSymbols(List<String> tickerSymbols) {
		tickerSymbols.stream()
			.filter(tickerSymbol -> !currentPriceManager.hasKey(tickerSymbol))
			.forEach(currentPriceManager::addKey);
	}

	public void removePortfolioSubscription(String sessionId) {
		portfolioSubscriptionManager.removePortfolioSubscription(sessionId);
	}

	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
	public void publishPortfolioDetail() {
		List<CompletableFuture<PortfolioHoldingsResponse>> futures = portfolioSubscriptionManager.values()
			.parallelStream()
			.filter(this::hasAllCurrentPrice)
			.map(PortfolioSubscription::getPortfolioId)
			.map(portfolioId -> CompletableFuture.supplyAsync(
					() -> portfolioStockService.readMyPortfolioStocks(portfolioId), portfolioDetailExecutor)
				.exceptionally(e -> {
					log.info(e.getMessage(), e);
					return null;
				}))
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

	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
	@Transactional(readOnly = true)
	public void refreshCurrentPrice() {
		List<String> tickerSymbols = portfolioRepository.findAll().parallelStream()
			.map(Portfolio::getPortfolioHoldings)
			.flatMap(Collection::stream)
			.map(PortfolioHolding::getStock)
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toList());
		refreshCurrentPrice(tickerSymbols);
		log.info("{}개의 종목 현재가 갱신 완료", tickerSymbols.size());
	}

	public void refreshCurrentPrice(List<String> tickerSymbols) {
		List<CompletableFuture<CurrentPriceResponse>> futures = tickerSymbols.parallelStream()
			.map(tickerSymbol -> {
				CompletableFuture<CurrentPriceResponse> future = new CompletableFuture<>();
				executorService.schedule(createCurrentPriceRequest(tickerSymbol, future), 200, TimeUnit.MILLISECONDS);
				return future;
			}).collect(Collectors.toList());

		futures.parallelStream()
			.map(CompletableFuture::join)
			.forEach(currentPriceManager::addCurrentPrice);
	}

	public Runnable createCurrentPriceRequest(final String tickerSymbol,
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
}
