package co.fineants.api.domain.portfolio.domain.dto.response;

import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioNotificationUpdateResponse {
	private Long portfolioId;
	private Boolean isActive;

	public static PortfolioNotificationUpdateResponse targetGainIsActive(Portfolio portfolio) {
		return new PortfolioNotificationUpdateResponse(portfolio.getId(), portfolio.getTargetGainIsActive());
	}

	public static PortfolioNotificationUpdateResponse maximumLossIsActive(Portfolio portfolio) {
		return new PortfolioNotificationUpdateResponse(portfolio.getId(), portfolio.getMaximumLossIsActive());
	}
}
