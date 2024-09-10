package co.fineants.price.domain.stockprice.repository;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import co.fineants.price.domain.stockprice.service.StockPriceDispatcher;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockPriceRepository {

	private static final Set<String> tickerSymbolSet = ConcurrentHashMap.newKeySet();
	private final StockPriceDispatcher dispatcher;

	public void save(String tickerSymbol) {
		if (tickerSymbolSet.contains(tickerSymbol)) {
			return;
		}
		tickerSymbolSet.add(tickerSymbol);
		dispatcher.dispatch(tickerSymbol);
	}

	public void saveAll(Collection<String> tickerSymbols) {
		tickerSymbols.stream()
			.filter(ticker -> !tickerSymbolSet.contains(ticker))
			.forEach(ticker -> {
				tickerSymbolSet.add(ticker);
				dispatcher.dispatch(ticker);
			});
	}

	public Set<String> findAll() {
		return tickerSymbolSet.stream().collect(Collectors.toUnmodifiableSet());
	}

	public int size() {
		return tickerSymbolSet.size();
	}

	public void clear() {
		tickerSymbolSet.clear();
	}
}