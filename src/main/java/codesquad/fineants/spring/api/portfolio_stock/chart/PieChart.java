package codesquad.fineants.spring.api.portfolio_stock.chart;

import java.util.List;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioPieChartItem;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PieChart {

	private final CurrentPriceManager manager;

	public List<PortfolioPieChartItem> createBy(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		return portfolio.createPieChart();
	}
}
