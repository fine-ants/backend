package codesquad.fineants.spring.api.dashboard.response;

import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
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
	private Money valuation;
	private Percentage weight;
	private Money totalGain;
	private Percentage totalGainRate;

	public static DashboardPieChartResponse create(Long id, String name, Expression valuation, Percentage weight,
		Expression totalGain, Percentage totalGainRate) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return new DashboardPieChartResponse(
			id,
			name,
			valuation.reduce(bank, to),
			weight,
			totalGain.reduce(bank, to),
			totalGainRate);
	}

	public static DashboardPieChartResponse of(Portfolio portfolio, Expression totalValuation) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return new DashboardPieChartResponse(
			portfolio.getId(),
			portfolio.getName(),
			portfolio.calculateTotalAsset().reduce(bank, to),
			portfolio.calculateTotalAsset().divide(totalValuation).toPercentage(Bank.getInstance(), Currency.KRW),
			portfolio.calculateTotalGain().reduce(bank, to),
			portfolio.calculateTotalGainRate().toPercentage(Bank.getInstance(), Currency.KRW)
		);
	}
}
