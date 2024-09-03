package co.fineants.api.domain.holding.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.holding.event.domain.PortfolioHoldingEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioHoldingEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	public void publishPortfolioHolding(String tickerSymbol) {
		eventPublisher.publishEvent(new PortfolioHoldingEvent(tickerSymbol));
	}
}
