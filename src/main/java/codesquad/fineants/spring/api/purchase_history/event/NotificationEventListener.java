package codesquad.fineants.spring.api.purchase_history.event;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import codesquad.fineants.spring.api.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

	private final NotificationService notificationService;

	// 매입 이력 이벤트가 발생하면 포트폴리오 목표수익률에 달성하면 푸시 알림
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PushNotificationEvent.class)
	public void notifyPortfolioTargetGainMessages(PushNotificationEvent event) {
		SendableParameter parameter = event.getValue();
		List<String> targetGainMessages = notificationService.notifyPortfolioTargetGainMessages(
			parameter.getPortfolioId(), parameter.getMemberId());
		log.info("매입 이력 이벤트로 인한 목표 수익률 달성 알림 결과 : {}", targetGainMessages);
	}

	// 매입 이력 이벤트가 발생하면 포트폴리오 최대손실율에 도달하면 푸시 알림
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PushNotificationEvent.class)
	public void notifyPortfolioMaxLossMessages(PushNotificationEvent event) {
		SendableParameter parameter = event.getValue();
		List<String> maxLossMessageIds = notificationService.notifyPortfolioMaxLossMessages(parameter.getPortfolioId(),
			parameter.getMemberId());
		log.info("매입 이력 이벤트로 인한 최대 손실율 달성 알림 결과 : {}", maxLossMessageIds);
	}
}
