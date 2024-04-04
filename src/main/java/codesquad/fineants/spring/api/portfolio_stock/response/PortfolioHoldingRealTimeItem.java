package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioHoldingRealTimeItem {
	private Long currentValuation;
	private Long currentPrice;
	private Long dailyChange;
	private Double dailyChangeRate;
	private Long totalGain;
	private Double totalReturnRate;

	public static PortfolioHoldingRealTimeItem of(PortfolioHolding portfolioHolding, Money lastDayClosingPrice) {
		return new PortfolioHoldingRealTimeItem(
			portfolioHolding.calculateCurrentValuation().getAmount().longValue(),
			portfolioHolding.getCurrentPrice().getAmount().longValue(),
			portfolioHolding.calculateDailyChange(lastDayClosingPrice).getAmount().longValue(),
			portfolioHolding.calculateDailyChangeRate(lastDayClosingPrice),
			portfolioHolding.calculateTotalGain().getAmount().longValue(),
			portfolioHolding.calculateTotalReturnRate());
	}
}
