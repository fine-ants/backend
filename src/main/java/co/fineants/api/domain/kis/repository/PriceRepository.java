package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.client.KisCurrentPrice;

public interface PriceRepository {
	void savePrice(KisCurrentPrice... currentPrices);

	Optional<Money> fetchPriceBy(String tickerSymbol);

	Optional<Money> fetchPriceBy(PortfolioHolding holding);

	Optional<Money> getCachedPrice(String tickerSymbol);
}
