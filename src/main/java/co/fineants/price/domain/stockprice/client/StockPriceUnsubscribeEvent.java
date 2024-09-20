package co.fineants.price.domain.stockprice.client;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;

@Getter
public class StockPriceUnsubscribeEvent extends ApplicationEvent {

	private StockPriceUnsubscribeEvent(WebSocketSession session) {
		super(session);
	}

	public static StockPriceUnsubscribeEvent from(WebSocketSession session) {
		return new StockPriceUnsubscribeEvent(session);
	}
}
