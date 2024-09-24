package co.fineants.price.domain.stockprice.client;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketApprovalKeyReissueEvent extends ApplicationEvent {
	public WebSocketApprovalKeyReissueEvent(@NotNull WebSocketSession session) {
		super(session);
	}
}
