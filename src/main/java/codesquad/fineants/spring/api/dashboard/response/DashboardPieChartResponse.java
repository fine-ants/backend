package codesquad.fineants.spring.api.dashboard.response;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.portfolio.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class DashboardPieChartResponse {
	private Long id;
	private String name;
	private Long valuation;
	private Double weight;
	private Long totalGain;
	private Double totalGainRate;

	public static DashboardPieChartResponse create(Long id, String name, Long valuation, Double weight, Long totalGain,
		Double totalGainRate) {
		return new DashboardPieChartResponse(id, name, valuation, weight, totalGain, totalGainRate);
	}

	public static DashboardPieChartResponse of(Portfolio portfolio, Money totalValuation) {
		return new DashboardPieChartResponse(
			portfolio.getId(),
			portfolio.getName(),
			portfolio.calculateTotalAsset().getAmount().longValue(),
			portfolio.calculateTotalAsset().divide(totalValuation).toPercentage(),
			portfolio.calculateTotalGain().getAmount().longValue(),
			portfolio.calculateTotalGainRate()
		);
	}
}
