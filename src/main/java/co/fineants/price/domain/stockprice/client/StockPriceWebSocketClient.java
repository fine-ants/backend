package co.fineants.price.domain.stockprice.client;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import co.fineants.api.domain.kis.properties.KisProperties;
import co.fineants.api.domain.kis.repository.WebSocketApprovalKeyRedisRepository;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.price.domain.stockprice.domain.event.WebSocketSessionConnectEvent;
import co.fineants.price.domain.stockprice.domain.factory.StockPriceWebSocketMessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceWebSocketClient {
	private final StockPriceWebSocketHandler stockPriceWebSocketHandler;
	private final WebSocketClient webSocketClient;
	private final ApplicationEventPublisher eventPublisher;
	private final KisProperties kisProperties;
	private final StockPriceWebSocketMessageFactory factory;
	private final WebSocketApprovalKeyRedisRepository repository;
	private final KisService kisService;
	@Value("${kis.websocket.auto-connect:true}")
	private boolean websocketAutoConnect;
	private WebSocketSession session = null;

	@PostConstruct
	private void init() {
		if (!websocketAutoConnect) {
			return;
		}
		connect(kisProperties.getWebsocketCurrentPriceUrl());
	}

	public void connect(@NotNull String url) {
		checkWebSocketApprovalKey();
		try {
			session = webSocketClient.execute(stockPriceWebSocketHandler, url).get();
			log.info("connect Session : {}, uri : {}", session, url);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Thread interrupted during connection, errorMessage={}", e.getMessage());
			throw new IllegalStateException("Thread was interrupted during WebSocket connection", e);
		} catch (ExecutionException e) {
			log.warn("Thread interrupted during connection, cause={}", e.getMessage());
			throw new IllegalStateException("WebSocket connection failed due to execution error", e);
		}
	}

	private void checkWebSocketApprovalKey() {
		repository.fetchApprovalKey().ifPresentOrElse(
			approvalKey -> log.info("Approval key already exists: {}", approvalKey),
			() -> {
				kisService.fetchApprovalKey().ifPresent(repository::saveApprovalKey);
				log.info("Approval key fetched and saved");
			}
		);
	}

	public boolean sendSubscribeMessage(String ticker) {
		return sendMessage(ticker, factory.currentPriceSubscribeMessage(ticker));
	}

	public boolean sendUnsubscribeMessage(String ticker) {
		return sendMessage(ticker, factory.currentPriceUnsubscribeMessage(ticker));
	}

	private boolean sendMessage(String ticker, WebSocketMessage<String> message) {
		if (!isConnect()) {
			log.info("WebSocket session is not open");
			return false;
		}
		try {
			session.sendMessage(message);
			log.info("StockPriceWebStockClient sendMessage, ticker={}", ticker);
			return true;
		} catch (Exception e) {
			log.error("StockPriceWebStockClient fail sendMessage, errorMessage={}", e.getMessage());
			closeSession(session, CloseStatus.SERVER_ERROR);
			publishSessionReconnectEvent(session);
			return false;
		}
	}

	private void closeSession(WebSocketSession session, CloseStatus status) {
		if (!isConnect()) {
			return;
		}
		try {
			session.close(status);
			log.info("close session={}", session);
		} catch (IOException ex) {
			log.error("StockPriceWebSocketClient fail close session, errorMessage={}", ex.getMessage());
			this.session = null;
		}
	}

	private void publishSessionReconnectEvent(@NotNull WebSocketSession session) {
		eventPublisher.publishEvent(WebSocketSessionConnectEvent.from(session));
	}

	public void disconnect(CloseStatus status) {
		closeSession(this.session, status);
	}

	public boolean isConnect() {
		if (session == null) {
			return false;
		}
		return session.isOpen();
	}
}
