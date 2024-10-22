package co.fineants.api.domain.holding.domain.dto.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.global.common.time.LocalDateTimeService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioHoldingsResponse {
	@Getter
	@JsonProperty
	private final PortfolioDetailResponse portfolioDetails;
	@JsonProperty
	private final List<PortfolioHoldingItem> portfolioHoldings;

	public static PortfolioHoldingsResponse of(Portfolio portfolio, PortfolioGainHistory history,
		List<PortfolioHolding> portfolioHoldings, Map<String, Money> lastDayClosingPriceMap,
		LocalDateTimeService localDateTimeService, PortfolioCalculator calculator) {
		PortfolioDetailResponse portfolioDetailResponse = PortfolioDetailResponse.of(portfolio, history,
			localDateTimeService, calculator);
		List<PortfolioHoldingItem> portfolioHoldingItems = portfolioHoldings.stream()
			.map(portfolioHolding -> PortfolioHoldingItem.from(portfolioHolding,
				lastDayClosingPriceMap.getOrDefault(portfolioHolding.getStock().getTickerSymbol(), Money.zero()),
				calculator))
			.toList();
		return new PortfolioHoldingsResponse(portfolioDetailResponse, portfolioHoldingItems);
	}

	public static PortfolioHoldingsResponse of(PortfolioDetailResponse portfolioDetail,
		List<PortfolioHoldingItem> portfolioHoldingItems) {
		return new PortfolioHoldingsResponse(portfolioDetail, portfolioHoldingItems);
	}

}
