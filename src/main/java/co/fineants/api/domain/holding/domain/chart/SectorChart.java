package co.fineants.api.domain.holding.domain.chart;

import java.util.List;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioSectorChartItem;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SectorChart {
	private final CurrentPriceRedisRepository manager;

	public List<PortfolioSectorChartItem> createBy(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		PortfolioCalculator calculator = new PortfolioCalculator();
		Expression balance = calculator.calBalanceBy(portfolio);
		Expression totalAsset = calculator.calTotalAssetBy(portfolio);
		return portfolio.createSectorChart(balance, totalAsset);
	}
}
