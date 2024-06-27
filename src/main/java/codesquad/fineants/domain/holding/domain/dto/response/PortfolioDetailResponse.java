package codesquad.fineants.domain.holding.domain.dto.response;

import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.gainhistory.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
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
		Currency to = Currency.KRW;
		return PortfolioDetailResponse.builder()
			.id(portfolio.getId())
			.securitiesFirm(portfolio.getSecuritiesFirm())
			.name(portfolio.getName())
			.budget(portfolio.getBudget())
			.targetGain(portfolio.getTargetGain())
			.targetReturnRate(portfolio.calculateTargetReturnRate().toPercentage(Bank.getInstance(), to))
			.maximumLoss(portfolio.getMaximumLoss())
			.maximumLossRate(portfolio.calculateMaximumLossRate().toPercentage(Bank.getInstance(), to))
			.currentValuation(portfolio.calculateTotalCurrentValuation().reduce(bank, to))
			.investedAmount(portfolio.calculateTotalInvestmentAmount().reduce(bank, to))
			.totalGain(portfolio.calculateTotalGain().reduce(bank, to))
			.totalGainRate(portfolio.calculateTotalGainRate().toPercentage(Bank.getInstance(), to))
			.dailyGain(portfolio.calculateDailyGain(history).reduce(bank, to))
			.dailyGainRate(portfolio.calculateDailyGainRate(history).toPercentage(Bank.getInstance(), to))
			.balance(portfolio.calculateBalance().reduce(bank, to))
			.annualDividend(portfolio.calculateAnnualDividend().reduce(bank, to))
			.annualDividendYield(
				portfolio.calculateAnnualDividendYield().toPercentage(Bank.getInstance(), to))
			.annualInvestmentDividendYield(
				portfolio.calculateAnnualInvestmentDividendYield().toPercentage(Bank.getInstance(), to))
			.provisionalLossBalance(Money.zero())
			.targetGainNotify(portfolio.getTargetGainIsActive())
			.maxLossNotify(portfolio.getMaximumLossIsActive())
			.build();
	}

}

