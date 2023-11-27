package codesquad.fineants.spring.api.portfolio_stock.response;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PortfolioChartResponse {
	private List<PortfolioPieChartItem> pieChart;
	private List<PortfolioDividendChartItem> dividendChart;
	private List<PortfolioSectorChartItemResponse> sectorChart;
}
