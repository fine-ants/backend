package codesquad.fineants.spring.api.portfolio_stock.response;

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
	private Double annualDividendYield;
	private Double annualInvestmentDividendYield;
	private Long provisionalLossBalance;
	private Boolean targetGainNotification;
	private Boolean maxLossNotification;

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
			.annualDividendYield(portfolio.calculateAnnualDividendYield().doubleValue())
			.annualInvestmentDividendYield(portfolio.calculateAnnualInvestmentDividendYield().doubleValue())
			.provisionalLossBalance(0L)
			.targetGainNotification(portfolio.getTargetGainIsActive())
			.maxLossNotification(portfolio.getMaximumLossIsActive())
			.build();
	}

}

