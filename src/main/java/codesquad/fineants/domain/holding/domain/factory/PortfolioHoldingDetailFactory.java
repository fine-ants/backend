package codesquad.fineants.domain.holding.domain.factory;

import java.util.List;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.holding.domain.dto.response.PortfolioHoldingItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioHoldingRealTimeItem;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioHoldingDetailFactory {

	private final CurrentPriceRedisRepository manager;
	private final ClosingPriceRepository closingPriceRepository;

	public List<PortfolioHoldingItem> createPortfolioHoldingItems(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);

		return portfolio.getPortfolioHoldings().stream()
			.map(portfolioHolding -> PortfolioHoldingItem.from(
					portfolioHolding,
					portfolioHolding.getLastDayClosingPrice(closingPriceRepository)
				)
			)
			.toList();
	}

	public List<PortfolioHoldingRealTimeItem> createPortfolioHoldingRealTimeItems(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		return portfolio.getPortfolioHoldings().stream()
			.map(portfolioHolding -> PortfolioHoldingRealTimeItem.of(
				portfolioHolding,
				portfolioHolding.getLastDayClosingPrice(closingPriceRepository)
			))
			.toList();
	}
}
