package codesquad.fineants.domain.holding.domain.chart;

import java.util.List;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.holding.domain.dto.response.PortfolioPieChartItem;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PieChart {

	private final CurrentPriceRedisRepository manager;

	public List<PortfolioPieChartItem> createBy(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		return portfolio.createPieChart();
	}
}
