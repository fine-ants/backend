package co.fineants.api.domain.portfolio.domain.dto.response;

import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioModifyResponse {
	private Long id;
	private String name;

	public static PortfolioModifyResponse from(Portfolio portfolio) {
		return new PortfolioModifyResponse(portfolio.getId(), portfolio.name());
	}
}
