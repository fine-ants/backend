package codesquad.fineants.spring.api.portfolio.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import codesquad.fineants.domain.page.ScrollPaginationCollection;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfoliosResponse {
	private final List<PortFolioItem> portfolios;
	private final boolean hasNext;
	private final Long nextCursor;

	public static PortfoliosResponse of(ScrollPaginationCollection<Portfolio> portfoliosScroll,
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap, CurrentPriceManager manager) {
		if (portfoliosScroll.isLastScroll()) {
			return PortfoliosResponse.newLastScroll(portfoliosScroll.getCurrentScrollItems(), portfolioGainHistoryMap,
				manager);
		}
		return newScrollHasNext(portfoliosScroll.getCurrentScrollItems(), portfolioGainHistoryMap,
			portfoliosScroll.getNextCursor().getId(), manager);
	}

	private static PortfoliosResponse newLastScroll(List<Portfolio> portfolios,
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap, CurrentPriceManager manager) {
		return newScrollHasNext(portfolios, portfolioGainHistoryMap, null, manager);
	}

	private static PortfoliosResponse newScrollHasNext(List<Portfolio> portfolios,
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap,
		Long nextCursor, CurrentPriceManager manager) {
		if (nextCursor != null) {
			return new PortfoliosResponse(getContents(portfolios, portfolioGainHistoryMap, manager), true,
				nextCursor);
		}
		return new PortfoliosResponse(getContents(portfolios, portfolioGainHistoryMap, manager), false, null);
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
