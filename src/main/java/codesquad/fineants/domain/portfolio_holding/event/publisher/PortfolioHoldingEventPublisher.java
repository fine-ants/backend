package codesquad.fineants.domain.portfolio_holding.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.portfolio_holding.event.domain.PortfolioHoldingEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioHoldingEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	public void publishPortfolioHolding(String tickerSymbol) {
		eventPublisher.publishEvent(new PortfolioHoldingEvent(tickerSymbol));
	}
}
