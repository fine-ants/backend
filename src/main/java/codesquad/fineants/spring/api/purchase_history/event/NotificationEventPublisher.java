package codesquad.fineants.spring.api.purchase_history.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {
	private final ApplicationEventPublisher publisher;

	public void publishEvent(Long portfolioId, Long memberId) {
		SendableParameter value = SendableParameter.create(portfolioId, memberId);
		publisher.publishEvent(new PushNotificationEvent(value));
	}
}
