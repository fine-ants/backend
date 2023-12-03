package codesquad.fineants.spring.api.portfolio_stock;

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
	private final RandomColorGenerator colorGenerator;

	public List<PortfolioPieChartItem> createBy(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		return portfolio.createPieChart(colorGenerator);
	}
}
