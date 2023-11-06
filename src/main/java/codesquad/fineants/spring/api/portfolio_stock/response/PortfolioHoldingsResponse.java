package codesquad.fineants.spring.api.portfolio_stock.response;

import java.util.List;
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
		List<PortfolioHolding> portfolioHoldings) {
		PortfolioDetailResponse portfolioDetailResponse = PortfolioDetailResponse.from(portfolio, history);
		List<PortfolioStockItem> portfolioStockItems = portfolioHoldings.stream()
			.map(PortfolioStockItem::from)
			.collect(Collectors.toList());
		return new PortfolioHoldingsResponse(portfolioDetailResponse, portfolioStockItems);
	}

	public Long getPortfolioId() {
		return portfolioDetails.getId();
	}
}
