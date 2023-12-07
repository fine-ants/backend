package codesquad.fineants.spring.api.dashboard.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OverviewResponse {
	private String username;
	private Long totalValuation;
	private Long totalInvestment;
	private Integer totalGainRate;
	private Long totalAnnualDividend;
	private Integer totalAnnualDividendYield;

	public static OverviewResponse of(String username, Long totalValuation, Long totalInvestment, Integer totalGainRate,
		Long totalAnnualDividend, Integer totalAnnualDividendYield) {
		return new OverviewResponse(username, totalValuation, totalInvestment, totalGainRate, totalAnnualDividend,
			totalAnnualDividendYield);
	}
}
