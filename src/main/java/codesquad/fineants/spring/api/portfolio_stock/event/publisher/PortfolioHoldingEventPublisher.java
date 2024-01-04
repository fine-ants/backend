package codesquad.fineants.spring.api.portfolio_stock.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.portfolio_stock.event.PortfolioHoldingEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioHoldingEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	public void publishPortfolioHolding(String tickerSymbol) {
		eventPublisher.publishEvent(new PortfolioHoldingEvent(tickerSymbol));
	}
}
