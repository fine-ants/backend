package codesquad.fineants.spring.api.portfolio_stock.response;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PortfolioDividendChartItem {
	private int month;
	private Long amount;

	public static PortfolioDividendChartItem empty(int month) {
		return create(month, 0L);
	}

	public static PortfolioDividendChartItem create(int month, Long amount) {
		return new PortfolioDividendChartItem(month, amount);
	}
}
