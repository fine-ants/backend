package co.fineants.api.domain.kis.client;

import java.io.IOException;
import java.net.URI;

import org.springframework.stereotype.Component;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import lombok.extern.slf4j.Slf4j;

@ClientEndpoint
@Component
@Slf4j
public class KisWebSocketClient {
	private Session session;

	public Session connect(URI endpointURI) {
		try {
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
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

	public void closeSession() {
		try {
			this.session.close();
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
