package co.fineants.price.domain.stockprice.domain.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;

@Getter
public class StockPriceSubscribeEvent extends ApplicationEvent {

	private final String ticker;

	private StockPriceSubscribeEvent(WebSocketSession session, String ticker) {
		super(session);
		this.ticker = ticker;
	}

	public static StockPriceSubscribeEvent from(WebSocketSession session, String ticker) {
		return new StockPriceSubscribeEvent(session, ticker);
	}
}
