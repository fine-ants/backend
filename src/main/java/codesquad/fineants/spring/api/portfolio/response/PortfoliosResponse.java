package codesquad.fineants.spring.api.portfolio.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfoliosResponse {
	private final List<PortFolioItem> portfolios;

	public static PortfoliosResponse of(List<Portfolio> portfolios,
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap,
		CurrentPriceManager manager) {
		return new PortfoliosResponse(getContents(portfolios, portfolioGainHistoryMap, manager));
	}

	private static List<PortFolioItem> getContents(List<Portfolio> portfolios,
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap, CurrentPriceManager manager) {
		return portfolios.stream()
			.map(portfolio -> {
				portfolio.changeCurrentPriceFromHoldings(manager);
				return PortFolioItem.of(portfolio, portfolioGainHistoryMap.get(portfolio));
			})
			.collect(Collectors.toList());
	}
}
