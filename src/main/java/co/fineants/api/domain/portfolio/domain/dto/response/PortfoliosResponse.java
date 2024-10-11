package co.fineants.api.domain.portfolio.domain.dto.response;

import java.util.List;
import java.util.Map;

import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class PortfoliosResponse {
	private List<PortFolioItem> portfolios;

	public static PortfoliosResponse of(List<Portfolio> portfolios,
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap,
		CurrentPriceRedisRepository manager, PortfolioCalculator calculator) {
		return new PortfoliosResponse(getContents(portfolios, portfolioGainHistoryMap, manager, calculator));
	}

	private static List<PortFolioItem> getContents(List<Portfolio> portfolios,
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap, CurrentPriceRedisRepository manager,
		PortfolioCalculator calculator) {
		return portfolios.stream()
			.map(portfolio -> PortFolioItem.of(portfolio, portfolioGainHistoryMap.get(portfolio), calculator))
			.toList();
	}
}
