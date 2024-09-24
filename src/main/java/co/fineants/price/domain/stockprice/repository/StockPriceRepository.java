package co.fineants.price.domain.stockprice.repository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import co.fineants.price.domain.stockprice.domain.StockPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StockPriceRepository {

	private static final Set<StockPrice> tickerSymbolSet = ConcurrentHashMap.newKeySet();

	public boolean save(String tickerSymbol) {
		return tickerSymbolSet.add(StockPrice.newInstance(tickerSymbol));
	}

	public Set<StockPrice> findAll() {
		return tickerSymbolSet.stream().collect(Collectors.toUnmodifiableSet());
	}

	public int size() {
		return tickerSymbolSet.size();
	}

	public void clear() {
		tickerSymbolSet.clear();
	}

	public void remove(String ticker) {
		tickerSymbolSet.remove(StockPrice.newInstance(ticker));
		log.info("remove ticker={}", ticker);
	}

	public boolean contains(String ticker) {
		return tickerSymbolSet.contains(StockPrice.newInstance(ticker));
	}

	public boolean canSubscribe(String ticker) {
		return tickerSymbolSet.size() < 20 && !tickerSymbolSet.contains(StockPrice.newInstance(ticker));
	}
}
