package co.fineants.price.domain.stockprice.client;

import java.io.IOException;

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
		} else if (isSubscribeInternalErrorMessage(payload)) {
			log.info("Received Message : {}", message.getPayload());
			// SUBSCRIBE INTERNAL ERROR 메시지의 경우에는 저장소에서 종목을 제거한다
			String ticker = parseTicker(message.getPayload().toString());
			eventPublisher.publishEvent(StockPriceDeleteEvent.from(ticker));
		} else if (isMaxSubscribeOverMessage(payload)) {
			log.info("Received Message : {}", message.getPayload());
			// MAX SUBSCRIBE OVER 메시지의 경우에는 저장소에서 우선순위가 낮은 종목에 대하여 구독 해제를 신청해야 합니다.
			eventPublisher.publishEvent(StockPriceUnsubscribeEvent.from(session));
			// MAX SUBSCRIBE OVER 메시지를 받게된 종목에 대하여 다시 구독 요청해야 합니다.
			String ticker = parseTicker(message.getPayload().toString());
			eventPublisher.publishEvent(StockPriceSubscribeEvent.from(session, ticker));
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

	private String parseTicker(String payload) {
		KisSubscribeResponse response = ObjectMapperUtil.deserialize(payload, KisSubscribeResponse.class);
		return response.getTrKey();
	}

	private boolean isPingPongMessage(@NotNull String message) {
		return message.contains("PINGPONG");
	}

	private void sendPongMessage(@NotNull WebSocketSession session, WebSocketMessage<?> message) {
		try {
			session.sendMessage(message);
		} catch (IOException e) {
			log.error("StockPriceWebStockClient fail send pong data, errorMessage={}", e.getMessage());
			handleSessionCloseAndReconnect(session);
		}
	}

	private void handleSessionCloseAndReconnect(@NotNull WebSocketSession session) {
		try {
			session.close(CloseStatus.SERVER_ERROR);
		} catch (IOException ex) {
			log.error("session can not close, errorMessage={}", ex.getMessage());
		}
		// 재연결 처리
		eventPublisher.publishEvent(WebSocketSessionConnectEvent.from(session));
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
