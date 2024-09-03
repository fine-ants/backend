package co.fineants.api.domain.holding.domain.dto.response;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioPieChartItem {
	private String name;
	private Money valuation;
	private Percentage weight;
	private Money totalGain;
	private Percentage totalGainRate;

	private PortfolioPieChartItem(String name, Money valuation, Percentage weight) {
		this(name, valuation, weight, Money.zero(), Percentage.zero());
	}

	public static PortfolioPieChartItem stock(String name, Expression valuation, Percentage weight,
		Expression totalGain, Percentage totalGainRate) {
		return new PortfolioPieChartItem(
			name,
			Bank.getInstance().toWon(valuation),
			weight,
			Bank.getInstance().toWon(totalGain),
			totalGainRate);
	}

	public static PortfolioPieChartItem cash(Percentage weight, Money balance) {
		return new PortfolioPieChartItem(
			"현금",
			balance,
			weight);
	}
}
