package codesquad.fineants.spring.api.portfolio_stock.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class PortfolioHoldingEvent extends ApplicationEvent {
	private final String tickerSymbol;

	public PortfolioHoldingEvent(String tickerSymbol) {
		super(System.currentTimeMillis());
		this.tickerSymbol = tickerSymbol;
	}
}
