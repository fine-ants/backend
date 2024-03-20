package codesquad.fineants.spring.api.portfolio_stock.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioPieChartItem {
	private String name;
	private Long valuation;
	private Double weight;
	private Long totalGain;
	private Double totalGainRate;

	private PortfolioPieChartItem(String name, Long valuation, Double weight) {
		this(name, valuation, weight, 0L, 0.0);
	}

	public static PortfolioPieChartItem stock(String name, Long valuation, Double weight, Long totalGain,
		Double totalGainRate) {
		return new PortfolioPieChartItem(name, valuation, weight, totalGain, totalGainRate);
	}

	public static PortfolioPieChartItem cash(Double weight, Long balance) {
		return new PortfolioPieChartItem(
			"현금",
			balance,
			weight);
	}
}
