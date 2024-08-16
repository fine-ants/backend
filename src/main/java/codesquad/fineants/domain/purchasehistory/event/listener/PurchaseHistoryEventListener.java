package codesquad.fineants.domain.purchasehistory.event.listener;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

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
	@TransactionalEventListener
	public CompletableFuture<PortfolioNotifyMessagesResponse> notifyTargetGainBy(PushNotificationEvent event) {
		PurchaseHistoryEventSendableParameter parameter = event.getValue();
		return CompletableFuture.supplyAsync(() ->
			(PortfolioNotifyMessagesResponse)notificationService.notifyTargetGain(parameter.getPortfolioId()));
	}

	// 매입 이력 이벤트가 발생하면 포트폴리오 최대손실율에 도달하면 푸시 알림
	@Async
	@TransactionalEventListener
	public CompletableFuture<PortfolioNotifyMessagesResponse> notifyMaxLoss(PushNotificationEvent event) {
		PurchaseHistoryEventSendableParameter parameter = event.getValue();
		return CompletableFuture.supplyAsync(() ->
			(PortfolioNotifyMessagesResponse)notificationService.notifyMaxLoss(parameter.getPortfolioId()));
	}
}
