package codesquad.fineants.spring.api.notification.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioPublisher {
	private final ApplicationEventPublisher publisher;

	public void publishCurrentPriceEvent() {
		publisher.publishEvent(new CurrentPriceEvent());
	}
}
