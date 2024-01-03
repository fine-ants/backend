package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortfolioDetailResponse {
	@Getter
	private Long id;
	private String securitiesFirm;
	private String name;
	private Long budget;
	private Long targetGain;
	private Integer targetReturnRate;
	private Long maximumLoss;
	private Integer maximumLossRate;
	private Long currentValuation;
	private Long investedAmount;
	private Long totalGain;
	private Integer totalGainRate;
	private Long dailyGain;
	private Integer dailyGainRate;
	private Long balance;
	private Long annualDividend;
	private Integer annualDividendYield;
	private Integer annualInvestmentDividendYield;
	private Long provisionalLossBalance;
	private Boolean targetGainNotification;
	private Boolean maxLossNotification;

	@Builder(access = AccessLevel.PRIVATE)
	private PortfolioDetailResponse(Long id, String securitiesFirm, String name, Long budget, Long targetGain,
		Integer targetReturnRate, Long maximumLoss, Integer maximumLossRate, Long currentValuation, Long investedAmount,
		Long totalGain, Integer totalGainRate, Long dailyGain, Integer dailyGainRate, Long balance,
		Long annualDividend, Integer annualDividendYield, Integer annualInvestmentDividendYield,
		Long provisionalLossBalance, Boolean targetGainNotification, Boolean maxLossNotification) {
		this.id = id;
		this.securitiesFirm = securitiesFirm;
		this.name = name;
		this.budget = budget;
		this.targetGain = targetGain;
		this.targetReturnRate = targetReturnRate;
		this.maximumLoss = maximumLoss;
		this.maximumLossRate = maximumLossRate;
		this.currentValuation = currentValuation;
		this.investedAmount = investedAmount;
		this.totalGain = totalGain;
		this.totalGainRate = totalGainRate;
		this.dailyGain = dailyGain;
		this.dailyGainRate = dailyGainRate;
		this.balance = balance;
		this.annualDividend = annualDividend;
		this.annualDividendYield = annualDividendYield;
		this.annualInvestmentDividendYield = annualInvestmentDividendYield;
		this.provisionalLossBalance = provisionalLossBalance;
		this.targetGainNotification = targetGainNotification;
		this.maxLossNotification = maxLossNotification;
	}

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
			.annualInvestmentDividendYield(portfolio.calculateAnnualDividendYield())
			.annualInvestmentDividendYield(portfolio.calculateAnnualInvestmentDividendYield())
			.provisionalLossBalance(0L)
			.targetGainNotification(portfolio.getTargetGainIsActive())
			.maxLossNotification(portfolio.getMaximumIsActive())
			.build();
	}

}

