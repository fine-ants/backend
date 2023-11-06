package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import lombok.AccessLevel;
import lombok.Builder;

public class PortfolioHoldingDetailItem {
	private Long portfolioHoldingId;
	private Long currentValuation;
	private Long currentPrice;
	private Double averageCostPerShare;
	private Long numShares;
	private Long dailyChange;
	private Integer dailyChangeRate;
	private Long totalGain;
	private Integer totalReturnRate;
	private Long annualDividend;
	private Integer annualDividendYield;

	@Builder(access = AccessLevel.PRIVATE)
	private PortfolioHoldingDetailItem(Long portfolioHoldingId, Long currentValuation, Long currentPrice,
		Double averageCostPerShare, Long numShares, Long dailyChange, Integer dailyChangeRate, Long totalGain,
		Integer totalReturnRate, Long annualDividend, Integer annualDividendYield) {
		this.portfolioHoldingId = portfolioHoldingId;
		this.currentValuation = currentValuation;
		this.currentPrice = currentPrice;
		this.averageCostPerShare = averageCostPerShare;
		this.numShares = numShares;
		this.dailyChange = dailyChange;
		this.dailyChangeRate = dailyChangeRate;
		this.totalGain = totalGain;
		this.totalReturnRate = totalReturnRate;
		this.annualDividend = annualDividend;
		this.annualDividendYield = annualDividendYield;
	}

	public static PortfolioHoldingDetailItem from(PortfolioHolding portfolioHolding) {
		return PortfolioHoldingDetailItem.builder()
			.portfolioHoldingId(portfolioHolding.getId())
			.currentValuation(portfolioHolding.calculateCurrentValuation())
			.currentPrice(portfolioHolding.getCurrentPrice())
			.averageCostPerShare(portfolioHolding.calculateAverageCostPerShare())
			.numShares(portfolioHolding.calculateNumShares())
			.dailyChange(portfolioHolding.calculateDailyChange())
			.dailyChangeRate(portfolioHolding.calculateDailyChangeRate())
			.totalGain(portfolioHolding.calculateTotalGain())
			.totalReturnRate(portfolioHolding.calculateTotalReturnRate())
			.annualDividend(portfolioHolding.calculateAnnualDividend())
			.annualDividendYield(portfolioHolding.calculateAnnualDividendYield())
			.build();
	}
}
