package codesquad.fineants.spring.api.portfolio_notification.response;

import codesquad.fineants.domain.portfolio.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioNotificationModifyResponse {
	private Long portfolioId;
	private Boolean isActive;

	public static PortfolioNotificationModifyResponse targetGainIsActive(Portfolio portfolio) {
		return new PortfolioNotificationModifyResponse(portfolio.getId(), portfolio.getTargetGainIsActive());
	}

	public static PortfolioNotificationModifyResponse maximumLossIsActive(Portfolio portfolio) {
		return new PortfolioNotificationModifyResponse(portfolio.getId(), portfolio.getMaximumIsActive());
	}
}
