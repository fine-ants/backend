package codesquad.fineants.spring.api.watch_list.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.portfolio_stock.event.PortfolioHoldingEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WatchStockEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	public void publishWatchStock(String tickerSymbol) {
		eventPublisher.publishEvent(new PortfolioHoldingEvent(tickerSymbol));
	}
}
