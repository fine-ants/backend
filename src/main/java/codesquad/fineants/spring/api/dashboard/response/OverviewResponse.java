package codesquad.fineants.spring.api.dashboard.response;

import codesquad.fineants.domain.common.money.Money;
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
	private Money totalValuation;
	private Money totalInvestment;
	private Money totalGain;
	private Double totalGainRate;
	private Money totalAnnualDividend;
	private Double totalAnnualDividendYield;

	public static OverviewResponse of(String username, Money totalValuation, Money totalInvestment, Money totalGain,
		Double totalGainRate, Money totalAnnualDividend, Double totalAnnualDividendYield) {
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
			.totalValuation(Money.zero())
			.totalInvestment(Money.zero())
			.totalGain(Money.zero())
			.totalGainRate(0.0)
			.totalAnnualDividend(Money.zero())
			.totalAnnualDividendYield(0.0)
			.build();
	}
}
