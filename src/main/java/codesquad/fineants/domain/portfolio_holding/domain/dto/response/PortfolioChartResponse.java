package codesquad.fineants.domain.portfolio_holding.domain.dto.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class PortfolioChartResponse {
	private List<PortfolioPieChartItem> pieChart;
	private List<PortfolioDividendChartItem> dividendChart;
	private List<PortfolioSectorChartItem> sectorChart;

	public static PortfolioChartResponse create(
		List<PortfolioPieChartItem> pieChart,
		List<PortfolioDividendChartItem> dividendChart,
		List<PortfolioSectorChartItem> sectorChart) {
		return new PortfolioChartResponse(pieChart, dividendChart, sectorChart);
	}
}
