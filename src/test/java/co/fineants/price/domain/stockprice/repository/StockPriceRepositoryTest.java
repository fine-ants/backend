package co.fineants.price.domain.stockprice.repository;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.price.domain.stockprice.domain.StockPrice;

class StockPriceRepositoryTest {

	@DisplayName("기존 종목이 있는 상태에서 만료시간을 갱신한다")
	@Test
	void refreshExpiration() {
		// given
		StockPriceRepository repository = new StockPriceRepository();
		String ticker = "005930";
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiredTime = now.minusMinutes(1);
		StockPrice stockPrice = StockPrice.newInstance(ticker, expiredTime);
		repository.save(stockPrice);

		StockPrice refreshedStockPrice = StockPrice.newInstance(ticker, now.plusMinutes(1));
		// when
		repository.refreshExpiration(refreshedStockPrice);
		// then
		Assertions.assertThat(repository.findAll().stream().noneMatch(StockPrice::isExpired))
			.as("all stock price is not expired")
			.isTrue();
	}

}
