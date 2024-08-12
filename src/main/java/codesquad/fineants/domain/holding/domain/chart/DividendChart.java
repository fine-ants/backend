package codesquad.fineants.domain.holding.domain.chart;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.holding.domain.dto.response.PortfolioDividendChartItem;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
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
