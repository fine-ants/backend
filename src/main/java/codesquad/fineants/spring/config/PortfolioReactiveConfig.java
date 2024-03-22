package codesquad.fineants.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import codesquad.fineants.domain.portfolio.reactive.PortfolioObservable;
import codesquad.fineants.spring.api.portfolio_stock.service.PortfolioHoldingService;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class PortfolioReactiveConfig {
	private final PortfolioHoldingService service;

	@Bean
	public PortfolioObservable portfolioObservable() {
		return new PortfolioObservable(service);
	}
}
