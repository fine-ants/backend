package codesquad.fineants.spring.api.portfolio_stock.response;

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

	public static PortfolioHoldingRealTimeItem of(PortfolioHolding portfolioHolding, Long lastDayClosingPrice) {
		return new PortfolioHoldingRealTimeItem(
			portfolioHolding.calculateCurrentValuation(),
			portfolioHolding.getCurrentPrice(),
			portfolioHolding.calculateDailyChange(lastDayClosingPrice),
			portfolioHolding.calculateDailyChangeRate(lastDayClosingPrice),
			portfolioHolding.calculateTotalGain(),
			portfolioHolding.calculateTotalReturnRate());
	}
}
