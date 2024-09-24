package co.fineants.price.domain.stockprice.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisWebSocketApprovalKey;
import co.fineants.api.domain.kis.properties.KisHeader;
import co.fineants.api.domain.kis.properties.kiscodevalue.imple.CustomerType;
import co.fineants.api.domain.kis.repository.WebSocketApprovalKeyRedisRepository;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceWebSocketClient {

	private static final String KIS_WEBSOCKET_CURRENT_PRICE_URI =
		"ws://ops.koreainvestment.com:21000/websocket/tryitout/H0STCNT0";

	private final StockPriceWebSocketHandler stockPriceWebSocketHandler;
	private final WebSocketApprovalKeyRedisRepository approvalKeyRepository;
	private final KisClient kisClient;
	private final DelayManager delayManager;
	private final WebSocketClient webSocketClient;
	private final ApplicationEventPublisher eventPublisher;
	@Value("${kis.websocket.auto-connect:true}")
	private boolean websocketAutoConnect;
	private WebSocketSession session = null;

	@PostConstruct
	private void init() {
		if (!websocketAutoConnect) {
			return;
		}
		connect();
	}

	private Optional<String> fetchApprovalKey() {
		return kisClient.fetchWebSocketApprovalKey()
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedAccessTokenDelay()))
			.onErrorResume(throwable -> {
				log.error(throwable.getMessage());
				return Mono.empty();
			})
			.log()
			.blockOptional(delayManager.timeout())
			.map(KisWebSocketApprovalKey::getApprovalKey);
	}

	public void connect() {
		fetchApprovalKeyIfEmpty();
		connect(KIS_WEBSOCKET_CURRENT_PRICE_URI);
	}

	private void fetchApprovalKeyIfEmpty() {
		String approvalKey = approvalKeyRepository.fetchApprovalKey().orElse(null);
		if (approvalKey == null) {
			this.fetchApprovalKey().ifPresent(approvalKeyRepository::saveApprovalKey);
		}
	}

	public void connect(String uri) {
		try {
			session = webSocketClient.execute(stockPriceWebSocketHandler, uri).get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Thread interrupted during connection, errorMessage={}", e.getMessage());
			throw new IllegalStateException("Thread was interrupted during WebSocket connection", e);
		} catch (ExecutionException e) {
			log.warn("Thread interrupted during connection, cause={}", e.getMessage());
			throw new IllegalStateException("WebSocket connection failed due to execution error", e);
		}
	}

	public boolean sendSubscribeMessage(String ticker) {
		return sendMessage(ticker, new TextMessage(createCurrentPriceSubscribeRequest(ticker)));
	}

	public boolean sendUnsubscribeMessage(String ticker) {
		return sendMessage(ticker, new TextMessage(createCurrentPriceUnsubscribeRequest(ticker)));
	}

	private boolean sendMessage(String ticker, WebSocketMessage<String> message) {
		if (session == null || !session.isOpen()) {
			log.info("WebSocket session is not open");
			return false;
		}
		try {
			session.sendMessage(message);
			log.info("StockPriceWebStockClient sendMessage, ticker={}", ticker);
			return true;
		} catch (Exception e) {
			log.error("StockPriceWebStockClient fail sendMessage, errorMessage={}", e.getMessage());
			handleSessionCloseAndReconnect(session);
			return false;
		}
	}

	private void handleSessionCloseAndReconnect(@NotNull WebSocketSession session) {
		try {
			session.close(CloseStatus.SERVER_ERROR);
			log.info("close session={}", session);
		} catch (IOException ex) {
			log.error("StockPriceWebSocketClient fail close session, errorMessage={}", ex.getMessage());
		}
		// reconnect
		eventPublisher.publishEvent(WebSocketSessionConnectEvent.from(session));
	}

	private String createCurrentPriceSubscribeRequest(String ticker) {
		return createCurrentPriceRequest(ticker, "1");
	}

	private String createCurrentPriceUnsubscribeRequest(String ticker) {
		return createCurrentPriceRequest(ticker, "0");
	}

	private String createCurrentPriceRequest(String ticker, String trType) {
		Map<String, Object> requestMap = new HashMap<>();
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put(KisHeader.APPROVAL_KEY.name(), approvalKeyRepository.fetchApprovalKey().orElseThrow());
		headerMap.put(KisHeader.CUSTOMER_TYPE.getHeaderName(), CustomerType.INDIVIDUAL.getCode());
		headerMap.put(KisHeader.TR_TYPE.name(), trType);
		headerMap.put(KisHeader.CONTENT_TYPE.name(), "utf-8");

		Map<String, Object> bodyMap = new HashMap<>();
		Map<String, String> input = new HashMap<>();
		input.put("tr_id", "H0STCNT0");
		input.put("tr_key", ticker);
		bodyMap.put("input", input);

		requestMap.put("header", headerMap);
		requestMap.put("body", bodyMap);

		return ObjectMapperUtil.serialize(requestMap);
	}

	public void disconnect(CloseStatus status) {
		try {
			this.session.close(status);
		} catch (IOException e) {
			log.warn("StockPriceWebSocketClient fail close, error message is {}", e.getMessage());
			this.session = null;
		}
	}
}
