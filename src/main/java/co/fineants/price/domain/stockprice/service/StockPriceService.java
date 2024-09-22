package co.fineants.price.domain.stockprice.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import co.fineants.price.domain.stockprice.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockPriceService {
	private final StockPriceRepository repository;

	public void pushStocks(Set<String> tickerSymbols) {
		tickerSymbols.stream()
			.filter(repository::canSubscribe)
			.forEach(repository::save);
	}
}
