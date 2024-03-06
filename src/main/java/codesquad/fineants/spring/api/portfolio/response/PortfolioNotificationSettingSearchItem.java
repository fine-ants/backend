package codesquad.fineants.spring.api.portfolio.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.portfolio.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class PortfolioNotificationSettingSearchItem {
	private Long portfolioId;
	private String securitiesFirm;
	private String name;
	private Boolean targetGainNotify;
	private Boolean maxLossNotify;
	private LocalDateTime lastUpdated;

	public static PortfolioNotificationSettingSearchItem from(Portfolio portfolio) {
		return PortfolioNotificationSettingSearchItem.builder()
			.portfolioId(portfolio.getId())
			.securitiesFirm(portfolio.getSecuritiesFirm())
			.name(portfolio.getName())
			.targetGainNotify(portfolio.getTargetGainIsActive())
			.maxLossNotify(portfolio.getMaximumLossIsActive())
			.lastUpdated(portfolio.getModifiedAt())
			.build();
	}
}
