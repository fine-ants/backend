package codesquad.fineants.spring.api.kis.listener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import codesquad.fineants.spring.api.kis.KisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class KisStompEventListener {

	private final KisService kisService;

	@EventListener
	public void handleConnectEvent(SessionConnectedEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = accessor.getSessionId();
		log.info("스톰프 연결 이벤트, sessionId : {}", sessionId);
	}

	@EventListener
	public void handleDisconnectEvent(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = accessor.getSessionId();
		// 연결이 종료된 세션에 대한 추가 처리를 수행합니다.
		log.info("스톰프 연결 종료 이벤트, sessionId : {}", sessionId);
		kisService.removePortfolioSubscription(sessionId);
	}
}
