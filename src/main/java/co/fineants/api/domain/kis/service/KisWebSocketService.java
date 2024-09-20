package co.fineants.api.domain.kis.service;

import org.springframework.stereotype.Service;

import co.fineants.price.domain.stockprice.client.StockPriceWebSocketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisWebSocketService {
	private final StockPriceWebSocketClient stockPriceWebSocketClient;

	public void fetchCurrentPrice(String ticker) {
		stockPriceWebSocketClient.sendSubscribeMessage(ticker);
	}
}
