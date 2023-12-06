package codesquad.fineants.spring.api.portfolio_stock;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingRealTimeItem;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioHoldingDetailFactory {

	private final CurrentPriceManager currentPriceManager;
	private final LastDayClosingPriceManager lastDayClosingPriceManager;

	public List<PortfolioHoldingRealTimeItem> createPortfolioHoldingRealTimeItems(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(currentPriceManager);
		List<PortfolioHolding> portfolioHoldings = portfolio.getPortfolioHoldings();
		Map<String, Long> lastDayClosingPriceMap = portfolioHoldings.parallelStream()
			.map(PortfolioHolding::getStock)
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toMap(key -> key, lastDayClosingPriceManager::getPrice));
		return portfolio.getPortfolioHoldings().stream()
			.map(portfolioHolding -> PortfolioHoldingRealTimeItem.of(
				portfolioHolding,
				lastDayClosingPriceMap.getOrDefault(portfolioHolding.getStock().getTickerSymbol(), 0L)))
			.collect(Collectors.toList());
	}
}
