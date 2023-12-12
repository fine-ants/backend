package codesquad.fineants.spring.api.dashboard.response;

import codesquad.fineants.domain.portfolio.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DashboardPieChartResponse {
	private Long id;
	private String name;
	private Long valuation;
	private Double weight;
	private Long totalGain;
	private Integer totalGainRate;

	public static DashboardPieChartResponse of(Portfolio portfolio, Long totalValuation) {
		return new DashboardPieChartResponse(portfolio.getId(), portfolio.getName(), portfolio.calculateTotalAsset()
			, (double)(portfolio.calculateTotalAsset() / totalValuation) * 100, portfolio.calculateTotalGain(),
			portfolio.calculateTotalGainRate());
	}
}
