package codesquad.fineants.spring.api.portfolio_stock;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingRealTimeItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioStockItem;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioHoldingDetailFactory {

	private final CurrentPriceManager manager;
	private final LastDayClosingPriceManager lastDayClosingPriceManager;

	public List<PortfolioStockItem> createPortfolioStockItems(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);

		return portfolio.getPortfolioHoldings().stream()
			.map(portfolioHolding -> PortfolioStockItem.from(
				portfolioHolding,
				portfolioHolding.getLastDayClosingPrice(lastDayClosingPriceManager)))
			.collect(Collectors.toList());
	}

	public List<PortfolioHoldingRealTimeItem> createPortfolioHoldingRealTimeItems(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		return portfolio.getPortfolioHoldings().stream()
			.map(portfolioHolding -> PortfolioHoldingRealTimeItem.of(
				portfolioHolding,
				portfolioHolding.getLastDayClosingPrice(lastDayClosingPriceManager)
			))
			.collect(Collectors.toList());
	}
}
