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

	public boolean save(StockPrice stockPrice) {
		return tickerSymbolSet.add(stockPrice);
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

	public void remove(StockPrice stockPrice) {
		tickerSymbolSet.remove(stockPrice);
		log.info("remove stockPrice={}", stockPrice);
	}

	public boolean contains(StockPrice stockPrice) {
		return tickerSymbolSet.contains(stockPrice);
	}

	public boolean canSubscribe(StockPrice stockPrice) {
		return tickerSymbolSet.size() < 20 && !tickerSymbolSet.contains(stockPrice);
	}

	public void refreshExpiration(StockPrice stockPrice) {
		tickerSymbolSet.remove(stockPrice);
		tickerSymbolSet.add(stockPrice);
	}

	public Set<StockPrice> removeExpiredStockPrice() {
		Set<StockPrice> result = tickerSymbolSet.stream()
			.filter(StockPrice::isExpired)
			.collect(Collectors.toUnmodifiableSet());
		tickerSymbolSet.removeAll(result);
		return result;
	}
}
