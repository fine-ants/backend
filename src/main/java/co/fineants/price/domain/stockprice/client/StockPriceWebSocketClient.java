package co.fineants.price.domain.stockprice.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

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

	@Value("${kis.websocket.auto-connect:true}")
	private boolean websocketAutoConnect;
	private final WebSocketClient webSocketClient = new StandardWebSocketClient();
	private WebSocketSession session = null;

	@PostConstruct
	private void init() {
		if (!websocketAutoConnect) {
			return;
		}
		String approvalKey = approvalKeyRepository.fetchApprovalKey().orElse(null);
		if (approvalKey == null) {
			this.fetchApprovalKey().ifPresent(approvalKeyRepository::saveApprovalKey);
		}
		connect(KIS_WEBSOCKET_CURRENT_PRICE_URI);
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

	public boolean sendMessage(String ticker) {
		if (session != null && session.isOpen()) {
			try {
				log.info("StockPriceWebStockClient sendMessage, ticker={}", ticker);
				session.sendMessage(new TextMessage(createCurrentPriceRequest(ticker)));
				return true;
			} catch (IOException e) {
				log.error("StockPriceWebStockClient fail sendMessage, errorMessage={}", e.getMessage());
				return false;
			}
		} else {
			log.info("WebSocket session is not open");
			return false;
		}
	}

	private String createCurrentPriceRequest(String ticker) {
		Map<String, Object> requestMap = new HashMap<>();
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put(KisHeader.APPROVAL_KEY.name(), approvalKeyRepository.fetchApprovalKey().orElseThrow());
		headerMap.put(KisHeader.CUSTOMER_TYPE.getHeaderName(), CustomerType.INDIVIDUAL.getCode());
		headerMap.put(KisHeader.TR_TYPE.name(), "1");
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
}
