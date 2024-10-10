package co.fineants.api.domain.holding.domain.chart;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioSectorChartItem;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SectorChart {
	private final CurrentPriceRedisRepository manager;
	private final PortfolioCalculator calculator;

	public List<PortfolioSectorChartItem> createBy(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		Expression totalAsset = calculator.calTotalAssetBy(portfolio);

		Map<String, List<Expression>> sector = calculator.calSectorChartBy(portfolio);
		return sector.entrySet().stream()
			.map(entry -> {
				Expression currentValuation = entry.getValue().stream()
					.reduce(Money.wonZero(), Expression::plus);

				Percentage weightPercentage = calculator.calCurrentValuationWeight(currentValuation, totalAsset)
					.toPercentage(Bank.getInstance(), Currency.KRW);
				return PortfolioSectorChartItem.create(entry.getKey(), weightPercentage);
			})
			.sorted(PortfolioSectorChartItem::compareTo)
			.toList();
	}
}
