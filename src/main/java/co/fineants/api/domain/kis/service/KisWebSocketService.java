package co.fineants.api.domain.kis.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisWebSocketApprovalKey;
import co.fineants.api.domain.kis.client.KisWebSocketClient;
import co.fineants.api.domain.kis.properties.KisHeader;
import co.fineants.api.domain.kis.properties.KisTrIdProperties;
import co.fineants.api.domain.kis.properties.kiscodevalue.imple.CustomerType;
import co.fineants.api.domain.kis.repository.WebSocketApprovalKeyRedisRepository;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisWebSocketService {
	private static final String KIS_WEBSOCKET_CURRENT_PRICE_URI = "ws://ops.koreainvestment.com:21000/websocket/tryitout/H0STCNT0";
	private final KisWebSocketClient webSocketClient;
	private final KisClient kisClient;
	private final DelayManager delayManager;
	private final WebSocketApprovalKeyRedisRepository approvalKeyRepository;
	private final KisTrIdProperties kisTrIdProperties;

	@PostConstruct
	public void init() {
		String approvalKey = approvalKeyRepository.fetchApprovalKey().orElse(null);
		if (approvalKey == null) {
			this.fetchApprovalKey().ifPresent(approvalKeyRepository::saveApprovalKey);
		}
		webSocketClient.connect(createCurrentPriceUri());
	}

	private URI createCurrentPriceUri() {
		try {
			return new URI(KisWebSocketService.KIS_WEBSOCKET_CURRENT_PRICE_URI);
		} catch (URISyntaxException e) {
			log.error(e.getMessage());
			throw new IllegalArgumentException("invalid uri, " + KisWebSocketService.KIS_WEBSOCKET_CURRENT_PRICE_URI);
		}
	}

	public void fetchCurrentPrice(String ticker) {
		Map<String, Object> requestMap = new HashMap<>();
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put(KisHeader.APPROVAL_KEY.name(), approvalKeyRepository.fetchApprovalKey().orElseThrow());
		headerMap.put(KisHeader.CUSTOMER_TYPE.getHeaderName(), CustomerType.INDIVIDUAL.getCode());
		headerMap.put(KisHeader.TR_TYPE.name(), "1");
		headerMap.put(KisHeader.CONTENT_TYPE.name(), "utf-8");

		Map<String, Object> bodyMap = new HashMap<>();
		Map<String, String> input = new HashMap<>();
		input.put("tr_id", kisTrIdProperties.getWebsocketCurrentPrice());
		input.put("tr_key", ticker);
		bodyMap.put("input", input);

		requestMap.put("header", headerMap);
		requestMap.put("body", bodyMap);

		webSocketClient.sendMessage(ObjectMapperUtil.serialize(requestMap));
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
}
