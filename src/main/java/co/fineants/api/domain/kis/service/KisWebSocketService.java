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
	private final KisWebSocketClient webSocketClient;
	private final KisClient kisClient;
	private final DelayManager delayManager;
	private final WebSocketApprovalKeyRedisRepository approvalKeyRepository;

	@PostConstruct
	public void init() {
		fetchApprovalKey().ifPresent(approvalKeyRepository::saveApprovalKey);
	}

	public void fetchCurrentPrice(String ticker) {
		Map<String, Object> requestMap = new HashMap<>();
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("approval_key", approvalKeyRepository.fetchApprovalKey().orElseThrow());
		headerMap.put("custtype", CustomerType.INDIVIDUAL.getCode());
		headerMap.put("tr_type", "1");
		headerMap.put("content-type", "utf-8");

		Map<String, Object> bodyMap = new HashMap<>();
		Map<String, String> input = new HashMap<>();
		input.put("tr_id", "H0STCNT0");
		input.put("tr_key", ticker);
		bodyMap.put("input", input);

		requestMap.put("header", headerMap);
		requestMap.put("body", bodyMap);

		String webSocketUri = "ws://ops.koreainvestment.com:21000/tryitout/H0STCNT0";
		try {
			webSocketClient.connect(new URI(webSocketUri));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
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
