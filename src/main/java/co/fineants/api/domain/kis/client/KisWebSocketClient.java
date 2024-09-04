package co.fineants.api.domain.kis.client;

import java.net.URI;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.kis.domain.dto.response.KisSubscribeResponse;
import co.fineants.api.domain.kis.factory.WebSocketContainerProviderFactory;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.global.util.ObjectMapperUtil;
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
	private final CurrentPriceRedisRepository currentPriceRedisRepository;

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
		if (isSubscribeSuccessMessage(message)) {
			KisSubscribeResponse response = ObjectMapperUtil.deserialize(message, KisSubscribeResponse.class);
			log.debug("kisSubscribeResponse is {}", response);
		} else if (isRealTimeSigningPriceMessage(message)) {
			handleStockTextMessage(message);
		} else {
			log.info("Received Message : {}", message);
		}
	}

	// 실시간 체결가 메시지인지 여부 확인
	private boolean isRealTimeSigningPriceMessage(String message) {
		return message.startsWith("0|H0STCNT0");
	}

	private boolean isSubscribeSuccessMessage(String message) {
		return message.startsWith("{") && message.endsWith("}") && message.contains("SUBSCRIBE SUCCESS");
	}

	private void handleStockTextMessage(String message) {
		String[] parts = message.split("\\|");
		String[] values = parts[3].split("\\^");
		String ticker = values[0];
		String currentPrice = values[2];
		KisCurrentPrice kisCurrentPrice = KisCurrentPrice.create(ticker, Long.valueOf(currentPrice));
		currentPriceRedisRepository.savePrice(kisCurrentPrice);
		log.debug("save the stock currentPrice, {}", kisCurrentPrice);
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
