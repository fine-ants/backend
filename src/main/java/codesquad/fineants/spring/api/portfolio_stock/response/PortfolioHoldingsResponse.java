package codesquad.fineants.spring.api.portfolio_stock.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioHoldingsResponse {
	private PortfolioDetailResponse portfolioDetails;
	private List<PortfolioHoldingItem> portfolioHoldings;

	public static PortfolioHoldingsResponse of(Portfolio portfolio, PortfolioGainHistory history,
		List<PortfolioHolding> portfolioHoldings, Map<String, Long> lastDayClosingPriceMap) {
		PortfolioDetailResponse portfolioDetailResponse = PortfolioDetailResponse.from(portfolio, history);
		List<PortfolioHoldingItem> portfolioHoldingItems = portfolioHoldings.stream()
			.map(portfolioHolding -> PortfolioHoldingItem.from(portfolioHolding,
				lastDayClosingPriceMap.getOrDefault(portfolioHolding.getStock().getTickerSymbol(), 0L)))
			.collect(Collectors.toList());
		return new PortfolioHoldingsResponse(portfolioDetailResponse, portfolioHoldingItems);
	}

	public static PortfolioHoldingsResponse of(PortfolioDetailResponse portfolioDetail,
		List<PortfolioHoldingItem> portfolioHoldingItems) {
		return new PortfolioHoldingsResponse(portfolioDetail, portfolioHoldingItems);
	}

	public Long getPortfolioId() {
		return portfolioDetails.getId();
	}
}
