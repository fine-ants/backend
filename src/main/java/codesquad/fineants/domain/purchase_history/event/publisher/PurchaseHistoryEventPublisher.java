package codesquad.fineants.domain.purchase_history.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.purchase_history.event.aop.PurchaseHistoryEventSendableParameter;
import codesquad.fineants.domain.purchase_history.event.domain.PushNotificationEvent;
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
