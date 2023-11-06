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
	private Long investedAmount;
	private Long totalGain;
	private Integer totalGainRate;
	private Long dailyGain;
	private Integer dailyGainRate;
	private Long balance;
	private Long totalAnnualDividend;
	private Integer totalAnnualDividendYield;
	private Integer annualInvestmentDividendYield;
	private Long provisionalLossBalance;
	private Boolean targetGainNotification;
	private Boolean maxLossNotification;

	@Builder(access = AccessLevel.PRIVATE)
	private PortfolioDetailResponse(Long id, String securitiesFirm, String name, Long budget, Long targetGain,
		Integer targetReturnRate, Long maximumLoss, Integer maximumLossRate, Long investedAmount, Long totalGain,
		Integer totalGainRate, Long dailyGain, Integer dailyGainRate, Long balance, Long totalAnnualDividend,
		Integer totalAnnualDividendYield, Integer annualInvestmentDividendYield, Long provisionalLossBalance,
		Boolean targetGainNotification, Boolean maxLossNotification) {
		this.id = id;
		this.securitiesFirm = securitiesFirm;
		this.name = name;
		this.budget = budget;
		this.targetGain = targetGain;
		this.targetReturnRate = targetReturnRate;
		this.maximumLoss = maximumLoss;
		this.maximumLossRate = maximumLossRate;
		this.investedAmount = investedAmount;
		this.totalGain = totalGain;
		this.totalGainRate = totalGainRate;
		this.dailyGain = dailyGain;
		this.dailyGainRate = dailyGainRate;
		this.balance = balance;
		this.totalAnnualDividend = totalAnnualDividend;
		this.totalAnnualDividendYield = totalAnnualDividendYield;
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
			.investedAmount(portfolio.calculateTotalInvestmentAmount())
			.totalGain(portfolio.calculateTotalGain())
			.totalGainRate(portfolio.calculateTotalGainRate())
			.dailyGain(portfolio.calculateDailyGain(history))
			.dailyGainRate(portfolio.calculateDailyGainRate(history))
			.balance(portfolio.calculateBalance())
			.totalAnnualDividend(portfolio.calculateTotalAnnualDividend())
			.totalAnnualDividendYield(portfolio.calculateTotalAnnualDividendYield())
			.annualInvestmentDividendYield(portfolio.calculateAnnualInvestmentDividendYield())
			.provisionalLossBalance(0L)
			.targetGainNotification(portfolio.getTargetGainIsActive())
			.maxLossNotification(portfolio.getMaximumIsActive())
			.build();
	}

}

