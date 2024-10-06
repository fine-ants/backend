package co.fineants.api.domain.portfolio.domain.dto.response;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
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
		PortfolioCalculator calculator = new PortfolioCalculator();
		Expression totalGain = calculator.calTotalGainBy(portfolio);
		Expression totalGainRate = calculator.calTotalGainRateBy(portfolio);
		Expression totalAsset = calculator.calTotalAssetBy(portfolio);
		return new DashboardPieChartResponse(
			portfolio.getId(),
			portfolio.getName(),
			totalAsset.reduce(bank, to),
			totalAsset
				.divide(totalValuation)
				.toPercentage(Bank.getInstance(), Currency.KRW),
			totalGain.reduce(bank, to),
			totalGainRate.toPercentage(Bank.getInstance(), Currency.KRW)
		);
	}
}
