package codesquad.fineants.spring.api.portfolio_stock.response;

public class PortfolioPieChartItem {
	private String name;
	private Long valuation;
	private String fill;
	private Double weight;
	private Long totalGain;
	private Double totalGainRate;

	private PortfolioPieChartItem(String name, Long valuation, String fill, Double weight) {
		this(name, valuation, fill, weight, 0L, 0.0);
	}

	public PortfolioPieChartItem(String name, Long valuation, String fill, Double weight, Long totalGain,
		Double totalGainRate) {
		this.name = name;
		this.valuation = valuation;
		this.fill = fill;
		this.weight = weight;
		this.totalGain = totalGain;
		this.totalGainRate = totalGainRate;
	}

	public static PortfolioPieChartItem cash(Double weight, Long balance, String fill) {
		return new PortfolioPieChartItem(
			"현금",
			balance,
			fill,
			weight);
	}
}
