package codesquad.fineants.spring.api.dashboard.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OverviewResponse {
	private String username;
	private Long totalValuation;
	private Long totalInvestment;
	private Long totalGain;
	private Integer totalGainRate;
	private Long totalAnnualDividend;
	private Integer totalAnnualDividendYield;

	public static OverviewResponse of(String username, Long totalValuation, Long totalInvestment, Long totalGain,
		Integer totalGainRate, Long totalAnnualDividend, Integer totalAnnualDividendYield) {
		return OverviewResponse.builder()
			.username(username)
			.totalValuation(totalValuation)
			.totalInvestment(totalInvestment)
			.totalGain(totalGain)
			.totalGainRate(totalGainRate)
			.totalAnnualDividend(totalAnnualDividend)
			.totalAnnualDividendYield(totalAnnualDividendYield)
			.build();
	}

	public static OverviewResponse empty(String username) {
		return OverviewResponse.builder()
			.username(username)
			.totalValuation(0L)
			.totalInvestment(0L)
			.totalGain(0L)
			.totalGainRate(0)
			.totalAnnualDividend(0L)
			.totalAnnualDividendYield(0)
			.build();
	}
}
