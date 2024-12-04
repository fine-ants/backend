package co.fineants.api.domain.purchasehistory.event.listener;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.service.NotificationService;
import co.fineants.api.domain.purchasehistory.event.aop.PurchaseHistoryEventSendableParameter;
import co.fineants.api.domain.purchasehistory.event.domain.PushNotificationEvent;
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
	public CompletableFuture<List<NotifyMessageItem>> notifyTargetGainBy(PushNotificationEvent event) {
		PurchaseHistoryEventSendableParameter parameter = event.getValue();
		return CompletableFuture.supplyAsync(() -> notificationService.notifyTargetGain(parameter.getPortfolioId()));
	}

	// 매입 이력 이벤트가 발생하면 포트폴리오 최대손실율에 도달하면 푸시 알림
	@Async
	@TransactionalEventListener
	public CompletableFuture<List<NotifyMessageItem>> notifyMaxLoss(PushNotificationEvent event) {
		PurchaseHistoryEventSendableParameter parameter = event.getValue();
		return CompletableFuture.supplyAsync(() -> notificationService.notifyMaxLoss(parameter.getPortfolioId()));
	}
}
