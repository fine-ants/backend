package co.fineants.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestWebSocketHandler extends TextWebSocketHandler {

	private final Map<WebSocketSession, Boolean> sessionStatus = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessionStatus.put(session, true);

		String subscriptionSuccessMessage =
			"{\"header\":{\"tr_id\":\"H0STCNT0\",\"tr_key\":\"035720\",\"encrypt\":\"N\"},"
				+ "\"body\":{\"rt_cd\":\"0\",\"msg_cd\":\"OPSP0000\",\"msg1\":\"SUBSCRIBE SUCCESS\","
				+ "\"output\":{\"iv\":\"886b95bc1e1aa344\",\"key\":\"bbkldjxnnopzhieyozcwiahshtvsghqj\"}}}";
		session.sendMessage(new TextMessage(subscriptionSuccessMessage));
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		log.debug("payload : {}", message.getPayload());

		if (!sessionStatus.get(session)) {
			sessionStatus.put(session, true);
		} else {
			String marketDataMessage = "0|H0STCNT0|004|005930^145953^70100^5^-2400^-3.31^70326.47^69800^71100^69800"
				+ "^70100^70000^70^23528959^1654720632800^98690^164740^66050^62.27^13440861^8369764^1^0.36^144.22"
				+ "^090018^2^300^090850^5^-1000^090018^2^300^20240904^20^N^489757^526277^930360^3778585^0.39^13147340"
				+ "^178.96^0^^69800^005930^145953^70000^5^-2500^-3.45^70326.47^69800^71100^69800^70100^70000^1^23528960"
				+ "^1654720702800^98691^164740^66049^62.27^13440862^8369764^5^0.36^144";
			session.sendMessage(new TextMessage(marketDataMessage));
		}
	}
}
