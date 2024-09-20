package co.fineants.price.domain.stockprice.client;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;

@Getter
public class StockPriceUnsubscribeEvent extends ApplicationEvent {
	private final String ticker;

	private StockPriceUnsubscribeEvent(WebSocketSession session, String ticker) {
		super(session);
		this.ticker = ticker;
	}

	public static StockPriceUnsubscribeEvent from(WebSocketSession session, String ticker) {
		return new StockPriceUnsubscribeEvent(session, ticker);
	}
}
