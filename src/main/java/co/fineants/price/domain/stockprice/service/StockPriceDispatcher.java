package co.fineants.price.domain.stockprice.service;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.price.domain.stockprice.client.StockPriceWebSocketClient;
import lombok.RequiredArgsConstructor;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
public class StockPriceDispatcher {
	private final StockPriceWebSocketClient client;
	private final KisService kisService;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;

	private final Executor executor = Executors.newSingleThreadExecutor();

	public void dispatch(String ticker) {
		client.sendSubscribeMessage(ticker);
	}

	public void dispatchCurrentPrice(String ticker) {
		executor.execute(() ->
			kisService.fetchCurrentPrice(ticker)
				.retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(5)))
				.subscribe(currentPriceRedisRepository::savePrice));
	}
}
