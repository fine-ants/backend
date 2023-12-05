package codesquad.fineants.spring.api.portfolio_stock.response;

public class PortfolioPieChartItem {
	private String name;
	private Long valuation;
	private Double weight;
	private Long totalGain;
	private Double totalGainRate;

	private PortfolioPieChartItem(String name, Long valuation, Double weight) {
		this(name, valuation, weight, 0L, 0.0);
	}

	public PortfolioPieChartItem(String name, Long valuation, Double weight, Long totalGain,
		Double totalGainRate) {
		this.name = name;
		this.valuation = valuation;
		this.weight = weight;
		this.totalGain = totalGain;
		this.totalGainRate = totalGainRate;
	}

	public static PortfolioPieChartItem cash(Double weight, Long balance) {
		return new PortfolioPieChartItem(
			"현금",
			balance,
			weight);
	}
}
