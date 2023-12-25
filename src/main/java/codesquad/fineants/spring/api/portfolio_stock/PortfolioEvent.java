package codesquad.fineants.spring.api.portfolio_stock;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEvent;

import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsRealTimeResponse;
import lombok.Getter;

@Getter
public class PortfolioEvent extends ApplicationEvent {

	private final Long portfolioId;
	private final PortfolioHoldingsRealTimeResponse response;
	private final LocalDateTime eventDateTime;

	public PortfolioEvent(Long portfolioId, PortfolioHoldingsRealTimeResponse response, LocalDateTime eventDateTime) {
		super(System.currentTimeMillis());
		this.portfolioId = portfolioId;
		this.response = response;
		this.eventDateTime = eventDateTime;
	}
}
