package co.fineants.api.domain.holding.domain.chart;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioDividendChartItem;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DividendChart {

	private final CurrentPriceRedisRepository manager;

	public List<PortfolioDividendChartItem> createBy(Portfolio portfolio, LocalDate currentLocalDate) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		return portfolio.createDividendChart(currentLocalDate);
	}
}
