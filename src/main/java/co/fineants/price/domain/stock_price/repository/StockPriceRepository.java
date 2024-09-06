package co.fineants.price.domain.stock_price.repository;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

@Repository
public class StockPriceRepository {

	private static final Set<String> tickerSymbolSet = ConcurrentHashMap.newKeySet();

	public void save(String tickerSymbol) {
		tickerSymbolSet.add(tickerSymbol);
	}

	public void saveAll(Collection<String> tickerSymbols) {
		tickerSymbolSet.addAll(tickerSymbols);
	}

	public Set<String> findAll() {
		return tickerSymbolSet.stream().collect(Collectors.toUnmodifiableSet());
	}

	public int size() {
		return tickerSymbolSet.size();
	}
}
