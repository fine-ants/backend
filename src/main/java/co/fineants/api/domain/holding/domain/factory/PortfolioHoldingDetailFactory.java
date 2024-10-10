package co.fineants.api.domain.holding.domain.factory;

import java.util.List;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingRealTimeItem;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioHoldingDetailFactory {

	private final CurrentPriceRedisRepository manager;
	private final ClosingPriceRepository closingPriceRepository;
	private final PortfolioCalculator calculator;

	public List<PortfolioHoldingItem> createPortfolioHoldingItems(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);

		return portfolio.getPortfolioHoldings().stream()
			.map(portfolioHolding -> PortfolioHoldingItem.from(
				portfolioHolding,
				portfolioHolding.getLastDayClosingPrice(closingPriceRepository),
				calculator)
			)
			.toList();
	}

	public List<PortfolioHoldingRealTimeItem> createPortfolioHoldingRealTimeItems(Portfolio portfolio,
		PortfolioCalculator calculator) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		return portfolio.getPortfolioHoldings().stream()
			.map(portfolioHolding -> PortfolioHoldingRealTimeItem.of(
				portfolioHolding,
				portfolioHolding.getLastDayClosingPrice(closingPriceRepository),
				calculator
			))
			.toList();
	}
}
