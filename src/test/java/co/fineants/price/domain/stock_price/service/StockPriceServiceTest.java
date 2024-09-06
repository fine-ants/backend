package co.fineants.price.domain.stock_price.service;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.price.domain.stock_price.repository.StockPriceRepository;

class StockPriceServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private StockPriceService stockPriceService;

	@Autowired
	private StockPriceRepository stockPriceRepository;

	@DisplayName("사용자는 종목 정보를 큐에 넣는다")
	@Test
	void pushStocks() {
		// given
		List<String> tickerSymbols = List.of("005930", "035720");
		// when
		stockPriceService.pushStocks(tickerSymbols);
		// then
		Assertions.assertThat(stockPriceRepository.findAll()).hasSize(2);
	}
}
