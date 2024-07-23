package codesquad.fineants.domain.holding.domain.dto.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.gainhistory.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioHoldingsResponse {
	private PortfolioDetailResponse portfolioDetails;
	private List<PortfolioHoldingItem> portfolioHoldings;

	public static PortfolioHoldingsResponse of(Portfolio portfolio, PortfolioGainHistory history,
		List<PortfolioHolding> portfolioHoldings, Map<String, Money> lastDayClosingPriceMap) {
		PortfolioDetailResponse portfolioDetailResponse = PortfolioDetailResponse.from(portfolio, history);
		List<PortfolioHoldingItem> portfolioHoldingItems = portfolioHoldings.stream()
			.map(portfolioHolding -> PortfolioHoldingItem.from(portfolioHolding,
				lastDayClosingPriceMap.getOrDefault(portfolioHolding.getStock().getTickerSymbol(), Money.zero())))
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
