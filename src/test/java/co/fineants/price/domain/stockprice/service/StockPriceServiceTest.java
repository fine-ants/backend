package co.fineants.price.domain.stockprice.service;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.price.domain.stockprice.repository.StockPriceRepository;

class StockPriceServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private StockPriceService stockPriceService;

	@Autowired
	private StockPriceRepository stockPriceRepository;

	@DisplayName("사용자는 종목 정보를 큐에 넣는다")
	@Test
	void pushStocks() {
		// given
		Set<String> tickerSymbols = Set.of("005930", "035720");
		// when
		stockPriceService.pushStocks(tickerSymbols);
		// then
		Assertions.assertThat(stockPriceRepository.findAll()).hasSize(2);
	}
}
