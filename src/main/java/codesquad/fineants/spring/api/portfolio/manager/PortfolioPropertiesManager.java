package codesquad.fineants.spring.api.portfolio.manager;

import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.portfolio.properties.PortfolioProperties;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioPropertiesManager {
	private final PortfolioProperties portfolioProperties;

	public boolean contains(String securitiesFirm) {
		return portfolioProperties.contains(securitiesFirm);
	}
}
