package co.fineants.price.domain.stockprice.factory;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;

import co.fineants.api.domain.kis.properties.KisHeader;
import co.fineants.api.domain.kis.properties.kiscodevalue.imple.CustomerType;
import co.fineants.api.domain.kis.repository.WebSocketApprovalKeyRedisRepository;
import co.fineants.api.global.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockPriceWebSocketMessageFactory {

	private final WebSocketApprovalKeyRedisRepository approvalKeyRepository;

	public TextMessage currentPriceSubscribeMessage(String ticker) {
		String tradeType = "1";
		return new TextMessage(currentPriceRequest(ticker, tradeType));
	}

	public WebSocketMessage<String> currentPriceUnsubscribeMessage(String ticker) {
		String tradeType = "2";
		return new TextMessage(currentPriceRequest(ticker, tradeType));
	}

	private String currentPriceRequest(String ticker, String tradeType) {
		Map<String, Object> requestMap = new HashMap<>();
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put(KisHeader.APPROVAL_KEY.name(), approvalKeyRepository.fetchApprovalKey().orElseThrow());
		headerMap.put(KisHeader.CUSTOMER_TYPE.getHeaderName(), CustomerType.INDIVIDUAL.getCode());
		headerMap.put(KisHeader.TR_TYPE.name(), tradeType); // 거래 타입, 1:'등록', 2:'해제'
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
