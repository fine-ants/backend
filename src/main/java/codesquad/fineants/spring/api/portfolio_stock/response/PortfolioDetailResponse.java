package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
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

	public static PortfolioDetailResponse from(Portfolio portfolio, PortfolioGainHistory history) {
		Bank bank = Bank.getInstance();
		return PortfolioDetailResponse.builder()
			.id(portfolio.getId())
			.securitiesFirm(portfolio.getSecuritiesFirm())
			.name(portfolio.getName())
			.budget(portfolio.getBudget())
			.targetGain(portfolio.getTargetGain())
			.targetReturnRate(portfolio.calculateTargetReturnRate().toPercentage(Bank.getInstance(), Currency.KRW))
			.maximumLoss(portfolio.getMaximumLoss())
			.maximumLossRate(portfolio.calculateMaximumLossRate().toPercentage(Bank.getInstance(), Currency.KRW))
			.currentValuation(portfolio.calculateTotalCurrentValuation().reduce(bank, Currency.KRW))
			.investedAmount(portfolio.calculateTotalInvestmentAmount().reduce(bank, Currency.KRW))
			.totalGain(portfolio.calculateTotalGain().reduce(bank, Currency.KRW))
			.totalGainRate(portfolio.calculateTotalGainRate().toPercentage(Bank.getInstance(), Currency.KRW))
			.dailyGain(portfolio.calculateDailyGain(history).reduce(bank, Currency.KRW))
			.dailyGainRate(portfolio.calculateDailyGainRate(history).toPercentage(Bank.getInstance(), Currency.KRW))
			.balance(portfolio.calculateBalance().reduce(bank, Currency.KRW))
			.annualDividend(portfolio.calculateAnnualDividend())
			.annualDividendYield(
				portfolio.calculateAnnualDividendYield().toPercentage(Bank.getInstance(), Currency.KRW))
			.annualInvestmentDividendYield(
				portfolio.calculateAnnualInvestmentDividendYield().toPercentage(Bank.getInstance(), Currency.KRW))
			.provisionalLossBalance(Money.zero())
			.targetGainNotify(portfolio.getTargetGainIsActive())
			.maxLossNotify(portfolio.getMaximumLossIsActive())
			.build();
	}

}

