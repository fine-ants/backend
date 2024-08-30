package co.fineants.api.domain.portfolio.repository;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioPropertiesRepository {
	private final PortfolioProperties portfolioProperties;

	public boolean contains(String securitiesFirm) {
		return portfolioProperties.contains(securitiesFirm);
	}
}
