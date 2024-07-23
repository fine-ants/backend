package codesquad.fineants.domain.purchasehistory.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.purchasehistory.event.aop.PurchaseHistoryEventSendableParameter;
import codesquad.fineants.domain.purchasehistory.event.domain.PushNotificationEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PurchaseHistoryEventPublisher {
	private final ApplicationEventPublisher publisher;

	public void publishPushNotificationEvent(Long portfolioId, Long memberId) {
		publisher.publishEvent(new PushNotificationEvent(
			PurchaseHistoryEventSendableParameter.create(portfolioId, memberId)));
	}
}
