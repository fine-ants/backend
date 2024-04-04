package codesquad.fineants.spring.api.portfolio_stock.response;

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
	private Long currentValuation;
	private Long currentPrice;
	private Double averageCostPerShare;
	private Long numShares;
	private Long dailyChange;
	private Double dailyChangeRate;
	private Long totalGain;
	private Double totalReturnRate;
	private Long annualDividend;
	private Double annualDividendYield;

	public static PortfolioHoldingDetailItem from(PortfolioHolding portfolioHolding, Money lastDayClosingPrice) {
		return PortfolioHoldingDetailItem.builder()
			.portfolioHoldingId(portfolioHolding.getId())
			.currentValuation(portfolioHolding.calculateCurrentValuation().getAmount().longValue())
			.currentPrice(portfolioHolding.getCurrentPrice().getAmount().longValue())
			.averageCostPerShare(portfolioHolding.calculateAverageCostPerShare().getAmount().doubleValue())
			.numShares(portfolioHolding.calculateNumShares().getValue().longValue())
			.dailyChange(portfolioHolding.calculateDailyChange(lastDayClosingPrice).getAmount().longValue())
			.dailyChangeRate(portfolioHolding.calculateDailyChangeRate(lastDayClosingPrice))
			.totalGain(portfolioHolding.calculateTotalGain().getAmount().longValue())
			.totalReturnRate(portfolioHolding.calculateTotalReturnRate())
			.annualDividend(portfolioHolding.calculateAnnualExpectedDividend().getAmount().longValue())
			.annualDividendYield(portfolioHolding.calculateAnnualExpectedDividendYield())
			.build();
	}
}
