package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
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
	private Money currentValuation;
	private Money currentPrice;
	private Money averageCostPerShare;
	private Count numShares;
	private Money dailyChange;
	private Double dailyChangeRate;
	private Money totalGain;
	private Double totalReturnRate;
	private Money annualDividend;
	private Double annualDividendYield;

	public static PortfolioHoldingDetailItem from(PortfolioHolding portfolioHolding, Money lastDayClosingPrice) {
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
			.annualDividendYield(portfolioHolding.calculateAnnualExpectedDividendYield())
			.build();
	}
}
