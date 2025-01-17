package co.fineants.api.domain.holding.domain.chart;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDividendChartItem;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DividendChart {
	private final PortfolioCalculator calculator;

	public List<PortfolioDividendChartItem> createItemsBy(Portfolio portfolio, LocalDate currentLocalDate) {
		Map<Month, Expression> totalDividendMap = calculator.calTotalDividendBy(portfolio, currentLocalDate);

		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return totalDividendMap.entrySet().stream()
			.map(entry -> {
				Month month = entry.getKey();
				Money dividend = entry.getValue().reduce(bank, to);
				return PortfolioDividendChartItem.create(month.getValue(), dividend);
			})
			.sorted()
			.toList();
	}
}
