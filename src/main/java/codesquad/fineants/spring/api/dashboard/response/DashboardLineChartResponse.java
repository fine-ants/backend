package codesquad.fineants.spring.api.dashboard.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DashboardLineChartResponse {
	private String time;
	private Long value;

	public static DashboardLineChartResponse of(String time, Long value) {
		return new DashboardLineChartResponse(time, value);
	}
}
