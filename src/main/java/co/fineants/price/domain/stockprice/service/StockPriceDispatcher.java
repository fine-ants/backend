package co.fineants.price.domain.stockprice.service;

import java.util.Collection;

import org.springframework.stereotype.Component;

import co.fineants.price.domain.stockprice.client.StockPriceWebSocketClient;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockPriceDispatcher {
	private final StockPriceWebSocketClient client;

	public void dispatch(String ticker) {
		client.sendMessage(ticker);
	}

	public void dispatch(Collection<String> tickerSymbols) {
		tickerSymbols.forEach(client::sendMessage);
	}
}
