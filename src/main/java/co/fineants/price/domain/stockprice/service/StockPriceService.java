package co.fineants.price.domain.stockprice.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import co.fineants.price.domain.stockprice.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockPriceService {
	private final StockPriceRepository repository;
	private final StockPriceDispatcher dispatcher;

	public void pushStocks(Set<String> tickerSymbols) {
		for (String ticker : tickerSymbols) {
			// 구독 가능한 종목들에 대해서 종목 실시간 체결가에 대한 저장 및 구독 요청
			if (repository.canSubscribe(ticker)) {
				repository.save(ticker);
				dispatcher.dispatch(ticker);
			} else {
				// 구독 불가능한 종목들에 대해서 종목 현재가 조회 요청
				dispatcher.dispatchCurrentPrice(ticker);
			}
		}
	}
}
