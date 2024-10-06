package co.fineants.api.domain.holding.domain.dto.response;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.global.common.time.LocalDateTimeService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PortfolioDetailResponse {
	private Long id;
	private String securitiesFirm;
	private String name;
	private Money budget;
	private Money targetGain;
	private Percentage targetReturnRate;
	private Money maximumLoss;
	private Percentage maximumLossRate;
	private Money currentValuation;
	private Money investedAmount;
	private Money totalGain;
	private Percentage totalGainRate;
	private Money dailyGain;
	private Percentage dailyGainRate;
	private Money balance;
	private Money annualDividend;
	private Percentage annualDividendYield;
	private Percentage annualInvestmentDividendYield;
	private Money provisionalLossBalance;
	private Boolean targetGainNotify;
	private Boolean maxLossNotify;

	public static PortfolioDetailResponse from(Portfolio portfolio, PortfolioGainHistory history,
		LocalDateTimeService localDateTimeService) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		PortfolioCalculator calculator = new PortfolioCalculator();
		Expression totalGain = calculator.calTotalGainBy(portfolio);
		Expression totalGainRate = calculator.calTotalGainRateBy(portfolio);
		Expression totalInvestment = calculator.calTotalInvestmentBy(portfolio);
		Expression totalCurrentValuation = calculator.calTotalCurrentValuationBy(portfolio);
		Expression balance = calculator.calBalanceBy(portfolio);
		Expression dailyGain = calculator.calDailyGain(history, portfolio);
		Expression dailyGainRate = calculator.calDailyGainRateBy(history, portfolio);
		Expression annualDividend = calculator.calAnnualDividendBy(localDateTimeService, portfolio);
		Expression annualDividendYield = calculator.calAnnualDividendYieldBy(localDateTimeService, portfolio);
		return PortfolioDetailResponse.builder()
			.id(portfolio.getId())
			.securitiesFirm(portfolio.getSecuritiesFirm())
			.name(portfolio.getName())
			.budget(portfolio.getBudget())
			.targetGain(portfolio.getTargetGain())
			.targetReturnRate(portfolio.calculateTargetReturnRate().toPercentage(Bank.getInstance(), to))
			.maximumLoss(portfolio.getMaximumLoss())
			.maximumLossRate(portfolio.calculateMaximumLossRate().toPercentage(Bank.getInstance(), to))
			.currentValuation(totalCurrentValuation.reduce(bank, to))
			.investedAmount(totalInvestment.reduce(bank, to))
			.totalGain(totalGain.reduce(bank, to))
			.totalGainRate(totalGainRate.toPercentage(Bank.getInstance(), to))
			.dailyGain(dailyGain.reduce(bank, to))
			.dailyGainRate(dailyGainRate.toPercentage(Bank.getInstance(), to))
			.balance(balance.reduce(bank, to))
			.annualDividend(annualDividend.reduce(bank, to))
			.annualDividendYield(annualDividendYield.toPercentage(Bank.getInstance(), to))
			.annualInvestmentDividendYield(
				portfolio.calculateAnnualInvestmentDividendYield(localDateTimeService, totalInvestment)
					.toPercentage(Bank.getInstance(), to))
			.provisionalLossBalance(Money.zero())
			.targetGainNotify(portfolio.getTargetGainIsActive())
			.maxLossNotify(portfolio.getMaximumLossIsActive())
			.build();
	}

}

