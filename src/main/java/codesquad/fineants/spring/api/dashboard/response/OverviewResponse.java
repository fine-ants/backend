package codesquad.fineants.spring.api.dashboard.response;

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
public class OverviewResponse {
	private String username;
	private Long totalValuation;
	private Long totalInvestment;
	private Long totalGain;
	private Double totalGainRate;
	private Long totalAnnualDividend;
	private Double totalAnnualDividendYield;

	public static OverviewResponse of(String username, Long totalValuation, Long totalInvestment, Long totalGain,
		Double totalGainRate, Long totalAnnualDividend, Integer totalAnnualDividendYield) {
		return OverviewResponse.builder()
			.username(username)
			.totalValuation(totalValuation)
			.totalInvestment(totalInvestment)
			.totalGain(totalGain)
			.totalGainRate(totalGainRate)
			.totalAnnualDividend(totalAnnualDividend)
			.totalAnnualDividendYield(totalAnnualDividendYield.doubleValue())
			.build();
	}

	public static OverviewResponse empty(String username) {
		return OverviewResponse.builder()
			.username(username)
			.totalValuation(0L)
			.totalInvestment(0L)
			.totalGain(0L)
			.totalGainRate(0.0)
			.totalAnnualDividend(0L)
			.totalAnnualDividendYield(0.00)
			.build();
	}
}
