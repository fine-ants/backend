package co.fineants.price.domain.stock_price.service;

import java.util.List;

import org.springframework.stereotype.Service;

import co.fineants.price.domain.stock_price.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockPriceService {
	private final StockPriceRepository repository;

	public void pushStocks(List<String> tickerSymbols) {
		repository.saveAll(tickerSymbols);
	}
}
