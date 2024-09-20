package co.fineants.price.domain.stockprice.client;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

import lombok.ToString;

@ToString
public class WebSocketSessionEvent extends ApplicationEvent {
	private WebSocketSessionEvent(WebSocketSession session) {
		super(session);
	}

	public static WebSocketSessionEvent from(WebSocketSession session) {
		return new WebSocketSessionEvent(session);
	}
}
