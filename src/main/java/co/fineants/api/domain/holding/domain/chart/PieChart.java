package co.fineants.api.domain.holding.domain.chart;

import java.util.List;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PieChart {

	private final CurrentPriceRedisRepository manager;
	private final PortfolioCalculator calculator;

	public List<PortfolioPieChartItem> createItemsBy(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		return calculator.calCurrentValuationWeight(portfolio);
	}
}
