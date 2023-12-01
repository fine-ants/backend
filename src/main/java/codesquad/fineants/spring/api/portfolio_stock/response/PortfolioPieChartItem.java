package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.portfolio.Portfolio;
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

	public static PortfolioPieChartItem of(PortfolioHolding portfolioHolding, Long portfolioTotalAsset) {
		Long currentValuation = portfolioHolding.calculateCurrentValuation();
		Double weight = currentValuation.doubleValue() / portfolioTotalAsset.doubleValue() * 100;
		return new PortfolioPieChartItem(
			portfolioHolding.getStock().getCompanyName(),
			currentValuation,
			"#000000",
			weight,
			portfolioHolding.calculateTotalGain(),
			portfolioHolding.calculateTotalReturnRate().doubleValue()
		);
	}

	public static PortfolioPieChartItem cash(Portfolio portfolio) {
		Double weight =
			portfolio.calculateBalance().doubleValue() / portfolio.calculateTotalAsset().doubleValue() * 100;
		return new PortfolioPieChartItem(
			"현금",
			portfolio.calculateBalance(),
			"#1CADFF",
			weight,
			0L,
			0.0
		);
	}
}
