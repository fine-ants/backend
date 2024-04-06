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
	private Money currentValuation;
	private Money currentPrice;
	private Money dailyChange;
	private Double dailyChangeRate;
	private Money totalGain;
	private Double totalReturnRate;

	public static PortfolioHoldingRealTimeItem of(PortfolioHolding portfolioHolding, Money lastDayClosingPrice) {
		return new PortfolioHoldingRealTimeItem(
			portfolioHolding.calculateCurrentValuation(),
			portfolioHolding.getCurrentPrice(),
			portfolioHolding.calculateDailyChange(lastDayClosingPrice),
			portfolioHolding.calculateDailyChangeRate(lastDayClosingPrice),
			portfolioHolding.calculateTotalGain(),
			portfolioHolding.calculateTotalReturnRate());
	}
}
