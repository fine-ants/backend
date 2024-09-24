package co.fineants.price.domain.stockprice.client;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisSubscribeResponse;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.global.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceWebSocketHandler implements WebSocketHandler {

	private final CurrentPriceRedisRepository currentPriceRedisRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public void afterConnectionEstablished(@NotNull WebSocketSession session) {
		log.debug("StockPriceWebSocket connection established, session={}", session);
	}

	@Override
	public void handleMessage(@NotNull WebSocketSession session, WebSocketMessage<?> message) {
		String payload = message.getPayload().toString();
		if (isSubscribeSuccessMessage(payload)) {
			KisSubscribeResponse response = ObjectMapperUtil.deserialize(payload,
				KisSubscribeResponse.class);
			log.debug("kisSubscribeResponse is {}", response);
		} else if (isRealTimeSigningPriceMessage(payload)) {
			handleStockTextMessage(payload);
		} else if (isPingPongMessage(payload)) {
			sendPongMessage(session, message);
		} else if (isSubscribeInternalErrorMessage(payload) || isMaxSubscribeOverMessage(payload)) {
			log.info("Received Message : {}", message.getPayload());
		} else {
			log.info("Received Message : {}", message.getPayload());
		}
	}

	private boolean isMaxSubscribeOverMessage(String payload) {
		return payload.contains("MAX SUBSCRIBE OVER");
	}

	private boolean isSubscribeInternalErrorMessage(@NotNull String payload) {
		return payload.contains("SUBSCRIBE INTERNAL ERROR");
	}

	private boolean isPingPongMessage(@NotNull String message) {
		return message.contains("PINGPONG");
	}

	private void sendPongMessage(@NotNull WebSocketSession session, WebSocketMessage<?> message) {
		try {
			session.sendMessage(message);
		} catch (Exception e) {
			log.error("StockPriceWebStockClient fail send pong data, errorMessage={}", e.getMessage());
			closeSession(session);
			publishSessionReconnectEvent(session);
		}
	}

	private void closeSession(@NotNull WebSocketSession session) {
		try {
			session.close(CloseStatus.SERVER_ERROR);
		} catch (Exception ex) {
			log.error("StockPriceWebSocketHandler fail close session, errorMessage={}", ex.getMessage());
		}
	}

	private void publishSessionReconnectEvent(@NotNull WebSocketSession session) {
		eventPublisher.publishEvent(WebSocketSessionConnectEvent.from(session));
	}

	private boolean isRealTimeSigningPriceMessage(String message) {
		return message.startsWith("0|H0STCNT0");
	}

	private boolean isSubscribeSuccessMessage(String message) {
		return message.contains("SUBSCRIBE SUCCESS");
	}

	private void handleStockTextMessage(String message) {
		String[] parts = message.split("\\|");
		String[] values = parts[3].split("\\^");
		String ticker = values[0];
		String currentPrice = values[2];
		KisCurrentPrice kisCurrentPrice = KisCurrentPrice.create(ticker, Long.valueOf(currentPrice));
		currentPriceRedisRepository.savePrice(kisCurrentPrice);
		log.info("save the stock currentPrice, {}", kisCurrentPrice);
	}

	@Override
	public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {
		log.error("Error with websocket session={}, errorMessage={}", session, exception.getMessage());
	}

	@Override
	public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) {
		log.info("close StockPriceWebSocket session={}, closeStatus={}", session, closeStatus);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
}
