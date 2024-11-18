package co.fineants.price.domain.stockprice.scheduler;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;

import co.fineants.api.domain.kis.properties.KisProperties;
import co.fineants.price.domain.stockprice.client.StockPriceWebSocketClient;
import co.fineants.price.domain.stockprice.domain.StockPrice;
import co.fineants.price.domain.stockprice.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockPriceScheduler {
	private final StockPriceWebSocketClient client;
	private final StockPriceRepository repository;
	private final KisProperties kisProperties;

	@Scheduled(cron = "0 30 8 * * MON,TUE,WED,THU,FRI")
	public void openWebSocketClient() {
		log.info("open the StockPriceWebSocketClient");
		client.connect(kisProperties.getWebsocketCurrentPriceUrl());
	}

	@Scheduled(cron = "0 0 16 * * MON,TUE,WED,THU,FRI")
	public void closeWebSocketClient() {
		log.info("close the StockPriceWebSocketClient");
		client.disconnect(CloseStatus.NORMAL);
		repository.clear();
	}

	@Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
	public void removeExpiredStockPrice() {
		Set<StockPrice> delStockPrices = repository.removeExpiredStockPrice();
		log.info("remove the expired stock price, {}", delStockPrices);

		delStockPrices.forEach(stockPrice -> {
			String ticker = stockPrice.getTicker();
			boolean result = client.sendUnsubscribeMessage(ticker);
			if (result) {
				log.info("send the unsubscribe message, ticker={}, result={}", ticker, result);
			} else {
				repository.save(stockPrice);
			}
		});
	}
}
