package co.fineants.price.domain.stockprice.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import co.fineants.price.domain.stockprice.domain.StockPrice;
import co.fineants.price.domain.stockprice.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockPriceService {
	private final StockPriceRepository repository;
	private final StockPriceDispatcher dispatcher;

	public void pushStocks(Set<String> tickerSymbols) {
		for (String ticker : tickerSymbols) {
			StockPrice stockPrice = StockPrice.newInstance(ticker);
			if (repository.contains(stockPrice)) {
				// 종목이 이미 포함되어 있다면 만료시간 갱신
				repository.refreshExpiration(stockPrice);
			} else if (repository.canSubscribe(stockPrice)) {
				// 구독 가능한 종목들에 대해서 종목 실시간 체결가에 대한 저장 및 구독 요청
				repository.save(stockPrice);
				dispatcher.dispatch(ticker);
			} else {
				// 구독 불가능한 종목들에 대해서 종목 현재가 조회 요청
				dispatcher.dispatchCurrentPrice(ticker);
			}
		}
	}
}
