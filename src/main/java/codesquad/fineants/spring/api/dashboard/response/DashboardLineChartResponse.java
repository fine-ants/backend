package codesquad.fineants.spring.api.dashboard.response;

import codesquad.fineants.domain.common.money.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DashboardLineChartResponse {
	private String time;
	private Long value;

	public static DashboardLineChartResponse of(String time, Money value) {
		return new DashboardLineChartResponse(
			time,
			value.getAmount().longValue());
	}
}
