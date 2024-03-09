package codesquad.fineants.spring.api.portfolio_notification_setting.response;

import java.util.List;

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
public class PortfolioNotificationSettingSearchResponse {
	private List<PortfolioNotificationSettingSearchItem> portfolios;

	public static PortfolioNotificationSettingSearchResponse from(
		List<PortfolioNotificationSettingSearchItem> portfolios) {
		return PortfolioNotificationSettingSearchResponse.builder()
			.portfolios(portfolios)
			.build();
	}
}
