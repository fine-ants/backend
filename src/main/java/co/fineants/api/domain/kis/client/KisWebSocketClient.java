package co.fineants.api.domain.kis.client;

import java.net.URI;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.kis.factory.WebSocketContainerProviderFactory;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ClientEndpoint
@Component
@RequiredArgsConstructor
@Slf4j
public class KisWebSocketClient {
	private Session session;
	private final WebSocketContainerProviderFactory containerProviderFactory;

	public Session connect(URI endpointURI) {
		try {
			WebSocketContainer container = containerProviderFactory.getContainerProvider();
			return container.connectToServer(this, endpointURI);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		log.info("Connected to WebSocket Server, session={}", session);
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		log.info("closing websocket session={}, reason={}", session, reason);
		this.session = null;
	}

	@OnMessage
	public void onMessage(String message) {
		log.info("Received Message : {}", message);
	}

	public void sendMessage(String message) {
		if (session != null && session.isOpen()) {
			log.debug("sendText from session={}", session);
			session.getAsyncRemote().sendText(message);
		} else {
			log.info("WebSocket session is not open");
		}
	}
}
