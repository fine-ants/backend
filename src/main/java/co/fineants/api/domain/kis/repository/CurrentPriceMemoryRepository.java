package co.fineants.api.domain.kis.repository;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.client.KisCurrentPrice;

public class CurrentPriceMemoryRepository implements PriceRepository {

	private final Map<String, Long> store = new ConcurrentHashMap<>();

	@Override
	public void savePrice(KisCurrentPrice... prices) {
		Arrays.stream(prices).forEach(this::savePrice);
	}

	private void savePrice(KisCurrentPrice price) {
		store.put(price.toMemoryKey(), price.getPrice());
	}

	@Override
	public Optional<Money> fetchPriceBy(String tickerSymbol) {
		return getCachedPrice(tickerSymbol).map(Money::won);
	}

	public Optional<Long> getCachedPrice(String tickerSymbol) {
		return Optional.ofNullable(store.get(tickerSymbol));
	}
}
