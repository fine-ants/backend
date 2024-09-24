package co.fineants.price.domain.stockprice.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.kis.service.KisService;
import reactor.core.publisher.Mono;

class StockPriceDispatcherTest extends AbstractContainerBaseTest {

	@Autowired
	private StockPriceDispatcher dispatcher;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@MockBean
	private KisService kisService;

	@DisplayName("종목의 현재가를 조회하고 저장소에 캐시된다")
	@Test
	void dispatchCurrentPrice() {
		// given
		String ticker = "005930";
		given(kisService.fetchCurrentPrice(ticker))
			.willReturn(Mono.just(KisCurrentPrice.create(ticker, 50000L)));
		// when
		dispatcher.dispatchCurrentPrice(ticker);
		// then
		Awaitility.await()
			.atMost(1000, TimeUnit.MILLISECONDS)
			.untilAsserted(() -> assertThat(currentPriceRedisRepository.getCachedPrice(ticker)).isPresent());
	}
}
