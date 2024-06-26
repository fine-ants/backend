package codesquad.fineants.domain.purchasehistory.event.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.notification.domain.dto.response.PortfolioNotifyMessagesResponse;
import codesquad.fineants.domain.notification.service.NotificationService;
import codesquad.fineants.domain.purchasehistory.event.aop.PurchaseHistoryEventSendableParameter;
import codesquad.fineants.domain.purchasehistory.event.domain.PushNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseHistoryEventListener {

	private final NotificationService notificationService;

	// 매입 이력 이벤트가 발생하면 포트폴리오 목표수익률에 달성하면 푸시 알림
	@Async
	@EventListener
	public void notifyTargetGainBy(PushNotificationEvent event) {
		PurchaseHistoryEventSendableParameter parameter = event.getValue();
		PortfolioNotifyMessagesResponse response = (PortfolioNotifyMessagesResponse)notificationService.notifyTargetGainOf(
			parameter.getPortfolioId());
		log.info("매입 이력 이벤트로 인한 목표 수익률 달성 알림 결과 : {}", response);
	}

	// 매입 이력 이벤트가 발생하면 포트폴리오 최대손실율에 도달하면 푸시 알림
	@Async
	@EventListener
	public void notifyMaxLoss(PushNotificationEvent event) {
		PurchaseHistoryEventSendableParameter parameter = event.getValue();
		PortfolioNotifyMessagesResponse response = (PortfolioNotifyMessagesResponse)notificationService.notifyMaxLossOf(
			parameter.getPortfolioId());
		log.info("매입 이력 이벤트로 인한 최대 손실율 달성 알림 결과 : response={}", response);
	}
}
