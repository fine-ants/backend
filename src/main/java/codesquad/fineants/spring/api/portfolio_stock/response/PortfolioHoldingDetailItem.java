package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
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
	private Double annualDividendYield;

	public static PortfolioHoldingDetailItem from(PortfolioHolding portfolioHolding, long lastDayClosingPrice) {
		return PortfolioHoldingDetailItem.builder()
			.portfolioHoldingId(portfolioHolding.getId())
			.currentValuation(portfolioHolding.calculateCurrentValuation())
			.currentPrice(portfolioHolding.getCurrentPrice())
			.averageCostPerShare(portfolioHolding.calculateAverageCostPerShare())
			.numShares(portfolioHolding.calculateNumShares())
			.dailyChange(portfolioHolding.calculateDailyChange(lastDayClosingPrice))
			.dailyChangeRate(portfolioHolding.calculateDailyChangeRate(lastDayClosingPrice))
			.totalGain(portfolioHolding.calculateTotalGain())
			.totalReturnRate(portfolioHolding.calculateTotalReturnRate())
			.annualDividend(portfolioHolding.calculateAnnualExpectedDividend())
			.annualDividendYield(portfolioHolding.calculateAnnualExpectedDividendYield().doubleValue())
			.build();
	}
}
