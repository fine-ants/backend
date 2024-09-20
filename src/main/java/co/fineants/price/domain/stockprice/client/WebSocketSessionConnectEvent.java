package co.fineants.price.domain.stockprice.client;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

import lombok.ToString;

@ToString
public class WebSocketSessionConnectEvent extends ApplicationEvent {
	private WebSocketSessionConnectEvent(WebSocketSession session) {
		super(session);
	}

	public static WebSocketSessionConnectEvent from(WebSocketSession session) {
		return new WebSocketSessionConnectEvent(session);
	}
}
