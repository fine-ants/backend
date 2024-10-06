package co.fineants.api.domain.holding.domain.dto.response;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioDetailRealTimeItem {
	private Money currentValuation;
	private Money totalGain;
	private Percentage totalGainRate;
	private Money dailyGain;
	private Percentage dailyGainRate;
	private Money provisionalLossBalance;

	public static PortfolioDetailRealTimeItem of(Portfolio portfolio, PortfolioGainHistory history) {
		Bank bank = Bank.getInstance();
		PortfolioCalculator calculator = new PortfolioCalculator();
		Expression totalGain = calculator.calTotalGainBy(portfolio);
		Expression totalGainRate = calculator.calTotalGainRateBy(portfolio);
		Expression totalInvestment = calculator.calTotalInvestmentBy(portfolio);
		return new PortfolioDetailRealTimeItem(
			portfolio.calculateTotalCurrentValuation().reduce(bank, Currency.KRW),
			totalGain.reduce(bank, Currency.KRW),
			totalGainRate.toPercentage(Bank.getInstance(), Currency.KRW),
			portfolio.calculateDailyGain(history, totalInvestment).reduce(bank, Currency.KRW),
			portfolio.calculateDailyGainRate(history, totalInvestment).toPercentage(Bank.getInstance(), Currency.KRW),
			Money.zero()
		);
	}
}
