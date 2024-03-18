package codesquad.fineants.spring.api.purchase_history.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

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
