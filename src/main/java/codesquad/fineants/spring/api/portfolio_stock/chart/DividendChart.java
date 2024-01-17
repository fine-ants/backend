package codesquad.fineants.spring.api.portfolio_stock.chart;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioDividendChartItem;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DividendChart {

	private final CurrentPriceManager manager;

	public List<PortfolioDividendChartItem> createBy(Portfolio portfolio, LocalDate currentLocalDate) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		return portfolio.createDividendChart(currentLocalDate);
	}
}
