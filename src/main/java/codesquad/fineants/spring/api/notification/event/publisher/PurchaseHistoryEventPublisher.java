package codesquad.fineants.spring.api.notification.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.notification.event.PurchaseHistoryEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PurchaseHistoryEventPublisher {
	private final ApplicationEventPublisher publisher;

	public void publishPurchaseHistory(Long portfolioId, Long memberId) {
		publisher.publishEvent(new PurchaseHistoryEvent(portfolioId, memberId));
	}
}
