package codesquad.fineants.domain.portfolio_holding.domain.chart;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioDividendChartItem;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DividendChart {

	private final CurrentPriceRepository manager;

	public List<PortfolioDividendChartItem> createBy(Portfolio portfolio, LocalDate currentLocalDate) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		return portfolio.createDividendChart(currentLocalDate);
	}
}
