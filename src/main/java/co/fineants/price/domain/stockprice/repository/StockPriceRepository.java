package co.fineants.price.domain.stockprice.repository;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import co.fineants.price.domain.stockprice.service.StockPriceDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StockPriceRepository {

	private static final Set<String> tickerSymbolSet = ConcurrentHashMap.newKeySet();
	private final StockPriceDispatcher dispatcher;

	public void saveAll(Collection<String> tickerSymbols) {
		for (String ticker : tickerSymbols) {
			if (tickerSymbolSet.contains(ticker)) {
				continue;
			}
			tickerSymbolSet.add(ticker);
			dispatcher.dispatch(ticker);
		}
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
