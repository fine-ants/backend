package codesquad.fineants.domain.portfolio_holding.domain.chart;

import java.util.List;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioPieChartItem;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PieChart {

	private final CurrentPriceRepository manager;

	public List<PortfolioPieChartItem> createBy(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		return portfolio.createPieChart();
	}
}
