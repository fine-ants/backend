package co.fineants.price.domain.stockprice.repository;

import java.util.Collection;
import java.util.Optional;
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

	public Optional<String> pop() {
		return Optional.ofNullable(tickerSymbolSet.poll());
	}

	public void remove(String ticker) {
		log.info("remove ticker={}", ticker);
		tickerSymbolSet.remove(ticker);
	}
}
