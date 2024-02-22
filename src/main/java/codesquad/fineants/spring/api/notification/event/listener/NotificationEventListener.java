package codesquad.fineants.spring.api.notification.event.listener;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import codesquad.fineants.spring.api.notification.event.PurchaseHistoryEvent;
import codesquad.fineants.spring.api.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

	private final NotificationService notificationService;

	// 매입 이력 이벤트가 발생하면 포트폴리오 목표수익률에 달성했는지 검사 수행
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PurchaseHistoryEvent.class)
	public void listenPurchaseHistory(PurchaseHistoryEvent event) {
		List<String> targetGainMessages = notificationService.notifyPortfolioTargetGainMessages(event.getPortfolioId(),
			event.getMemberId());
		log.info("매입 이력 이벤트로 인한 목표 수익률 달성 알림 결과 : {}", targetGainMessages);

		List<String> maxLossMessageIds = notificationService.notifyPortfolioMaxLossMessages(event.getPortfolioId(),
			event.getMemberId());
		log.info("매입 이력 이벤트로 인한 최대 손실율 달성 알림 결과 : {}", maxLossMessageIds);
	}
}
