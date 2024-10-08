package co.fineants.api.domain.holding.domain.chart;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDividendChartItem;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DividendChart {

	private final CurrentPriceRedisRepository manager;

	public List<PortfolioDividendChartItem> createBy(Portfolio portfolio, LocalDate currentLocalDate) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		PortfolioCalculator calculator = new PortfolioCalculator();
		Map<Integer, Expression> totalDividendMap = calculator.calTotalDividendBy(portfolio, currentLocalDate);

		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return totalDividendMap.entrySet().stream()
			.map(entry -> PortfolioDividendChartItem.create(entry.getKey(), entry.getValue().reduce(bank, to)))
			.toList();
	}
}
