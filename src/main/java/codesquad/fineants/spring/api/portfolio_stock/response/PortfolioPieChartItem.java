package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioPieChartItem {
	private String name;
	private Long valuation;
	private String fill;
	private Double weight;
	private Long totalGain;
	private Double totalGainRate;

	public static PortfolioPieChartItem of(PortfolioHolding portfolioHolding, long currentValuation) {
		double weight = ((double)portfolioHolding.calculateCurrentValuation() / (double)currentValuation) * 100;
		return new PortfolioPieChartItem(
			portfolioHolding.getStock().getCompanyName(),
			currentValuation,
			"#000000",
			weight,
			portfolioHolding.calculateTotalGain(),
			portfolioHolding.calculateTotalReturnRate().doubleValue()
		);
	}
}
