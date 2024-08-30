package co.fineants.api.domain.portfolio.domain.dto.response;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
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
	private Percentage totalGainRate;
	private Money totalAnnualDividend;
	private Percentage totalAnnualDividendYield;

	public static OverviewResponse of(String username, Expression totalValuation, Expression totalInvestment,
		Expression totalGain, Percentage totalGainRate, Expression totalAnnualDividend,
		Percentage totalAnnualDividendYield) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return OverviewResponse.builder()
			.username(username)
			.totalValuation(totalValuation.reduce(bank, to))
			.totalInvestment(totalInvestment.reduce(bank, to))
			.totalGain(totalGain.reduce(bank, to))
			.totalGainRate(totalGainRate)
			.totalAnnualDividend(totalAnnualDividend.reduce(bank, to))
			.totalAnnualDividendYield(totalAnnualDividendYield)
			.build();
	}

	public static OverviewResponse empty(String username) {
		return OverviewResponse.builder()
			.username(username)
			.totalValuation(Money.zero())
			.totalInvestment(Money.zero())
			.totalGain(Money.zero())
			.totalGainRate(Percentage.zero())
			.totalAnnualDividend(Money.zero())
			.totalAnnualDividendYield(Percentage.zero())
			.build();
	}
}
