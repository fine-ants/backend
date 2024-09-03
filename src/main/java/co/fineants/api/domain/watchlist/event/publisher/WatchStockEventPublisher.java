package co.fineants.api.domain.watchlist.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.holding.event.domain.PortfolioHoldingEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WatchStockEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	public void publishWatchStock(String tickerSymbol) {
		eventPublisher.publishEvent(new PortfolioHoldingEvent(tickerSymbol));
	}
}
