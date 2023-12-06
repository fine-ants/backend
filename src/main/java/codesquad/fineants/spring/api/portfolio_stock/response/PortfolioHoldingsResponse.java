package codesquad.fineants.spring.api.portfolio_stock.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioHoldingsResponse {
	private PortfolioDetailResponse portfolioDetails;
	private List<PortfolioStockItem> portfolioHoldings;

	public static PortfolioHoldingsResponse of(Portfolio portfolio, PortfolioGainHistory history,
		List<PortfolioHolding> portfolioHoldings, Map<String, Long> lastDayClosingPriceMap) {
		PortfolioDetailResponse portfolioDetailResponse = PortfolioDetailResponse.from(portfolio, history);
		List<PortfolioStockItem> portfolioStockItems = portfolioHoldings.stream()
			.map(portfolioHolding -> PortfolioStockItem.from(portfolioHolding,
				lastDayClosingPriceMap.getOrDefault(portfolioHolding.getStock().getTickerSymbol(), 0L)))
			.collect(Collectors.toList());
		return new PortfolioHoldingsResponse(portfolioDetailResponse, portfolioStockItems);
	}

	public static PortfolioHoldingsResponse of(PortfolioDetailResponse portfolioDetail,
		List<PortfolioStockItem> portfolioStockItems) {
		return new PortfolioHoldingsResponse(portfolioDetail, portfolioStockItems);
	}

	public Long getPortfolioId() {
		return portfolioDetails.getId();
	}
}
