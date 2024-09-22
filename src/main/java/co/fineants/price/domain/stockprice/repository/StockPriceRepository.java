package co.fineants.price.domain.stockprice.repository;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import co.fineants.price.domain.stockprice.service.StockPriceDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StockPriceRepository {

	private static final Queue<String> tickerSymbolSet = new ConcurrentLinkedQueue<>();
	private final StockPriceDispatcher dispatcher;

	public void save(String tickerSymbol) {
		tickerSymbolSet.add(tickerSymbol);
		dispatcher.dispatch(tickerSymbol);
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

	public void remove(String ticker) {
		tickerSymbolSet.remove(ticker);
		log.info("remove ticker={}", ticker);
	}

	public boolean contains(String ticker) {
		return tickerSymbolSet.contains(ticker);
	}

	public boolean canSubscribe(String ticker) {
		return tickerSymbolSet.size() < 20 && tickerSymbolSet.contains(ticker);
	}
}
