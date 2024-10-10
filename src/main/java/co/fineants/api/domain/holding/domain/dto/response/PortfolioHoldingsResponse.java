package co.fineants.api.domain.holding.domain.dto.response;

import java.util.List;
import java.util.Map;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.global.common.time.LocalDateTimeService;
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
		List<PortfolioHolding> portfolioHoldings, Map<String, Money> lastDayClosingPriceMap,
		LocalDateTimeService localDateTimeService, PortfolioCalculator calculator) {
		PortfolioDetailResponse portfolioDetailResponse = PortfolioDetailResponse.of(portfolio, history,
			localDateTimeService, calculator);
		List<PortfolioHoldingItem> portfolioHoldingItems = portfolioHoldings.stream()
			.map(portfolioHolding -> PortfolioHoldingItem.from(portfolioHolding,
				lastDayClosingPriceMap.getOrDefault(portfolioHolding.getStock().getTickerSymbol(), Money.zero())))
			.toList();
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
