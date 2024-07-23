package codesquad.fineants.domain.notification.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.notification.event.domain.CurrentPriceEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioPublisher {
	private final ApplicationEventPublisher publisher;

	public void publishCurrentPriceEvent() {
		publisher.publishEvent(new CurrentPriceEvent());
	}
}
