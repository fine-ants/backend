package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.common.money.Money;
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
	private Double targetReturnRate;
	private Money maximumLoss;
	private Double maximumLossRate;
	private Money currentValuation;
	private Money investedAmount;
	private Money totalGain;
	private Double totalGainRate;
	private Money dailyGain;
	private Double dailyGainRate;
	private Money balance;
	private Money annualDividend;
	private Double annualDividendYield;
	private Double annualInvestmentDividendYield;
	private Money provisionalLossBalance;
	private Boolean targetGainNotify;
	private Boolean maxLossNotify;

	public static PortfolioDetailResponse from(Portfolio portfolio, PortfolioGainHistory history) {
		return PortfolioDetailResponse.builder()
			.id(portfolio.getId())
			.securitiesFirm(portfolio.getSecuritiesFirm())
			.name(portfolio.getName())
			.budget(portfolio.getBudget())
			.targetGain(portfolio.getTargetGain())
			.targetReturnRate(portfolio.calculateTargetReturnRate())
			.maximumLoss(portfolio.getMaximumLoss())
			.maximumLossRate(portfolio.calculateMaximumLossRate())
			.currentValuation(portfolio.calculateTotalCurrentValuation())
			.investedAmount(portfolio.calculateTotalInvestmentAmount())
			.totalGain(portfolio.calculateTotalGain())
			.totalGainRate(portfolio.calculateTotalGainRate())
			.dailyGain(portfolio.calculateDailyGain(history))
			.dailyGainRate(portfolio.calculateDailyGainRate(history))
			.balance(portfolio.calculateBalance())
			.annualDividend(portfolio.calculateAnnualDividend())
			.annualDividendYield(portfolio.calculateAnnualDividendYield())
			.annualInvestmentDividendYield(portfolio.calculateAnnualInvestmentDividendYield())
			.provisionalLossBalance(Money.zero())
			.targetGainNotify(portfolio.getTargetGainIsActive())
			.maxLossNotify(portfolio.getMaximumLossIsActive())
			.build();
	}

}

