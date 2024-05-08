package codesquad.fineants.domain.portfolio_holding.domain.chart;

import java.util.List;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioSectorChartItem;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SectorChart {
	private final CurrentPriceRepository manager;

	public List<PortfolioSectorChartItem> createBy(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		return portfolio.createSectorChart();
	}
}
