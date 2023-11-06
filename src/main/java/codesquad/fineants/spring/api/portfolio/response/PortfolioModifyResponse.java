package codesquad.fineants.spring.api.portfolio.response;

import codesquad.fineants.domain.portfolio.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioModifyResponse {
	private Long id;
	private String name;

	public static PortfolioModifyResponse from(Portfolio portfolio) {
		return new PortfolioModifyResponse(portfolio.getId(), portfolio.getName());
	}
}
