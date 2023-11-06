package codesquad.fineants.spring.api.kis;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class KisServiceTest {

	@Autowired
	private KisService kisService;

	@MockBean
	private CurrentPriceManager currentPriceManager;

	@Autowired
	private KisAccessTokenManager manager;
	@Autowired
	private KisClient client;
	@Autowired
	private KisRedisService redisService;

	@DisplayName("주식 현재가 시세를 가져온다")
	@Test
	void readRealTimeCurrentPrice() {
		// given
		String tickerSymbol = "005930";
		// when
		kisService.readRealTimeCurrentPrice(tickerSymbol);
		// then
	}

	@DisplayName("1초마다 주식 현재가 시세를 요청하고 갱신합니다.")
	@Test
	void publishCurrentPrice() throws InterruptedException {
		// given

		// when
		kisService.addTickerSymbols(List.of("005930", "035720"));
		Thread.sleep(20000L);
		// then
	}

	@DisplayName("AccessTokenAspect이 수행하여 새로운 엑세스 토큰을 갱신한다")
	@Test
	void readRealTimeCurrentPriceWithAspect() {
		// given
		String tickerSymbol = "005930";
		// when
		CurrentPriceResponse response = kisService.readRealTimeCurrentPrice(tickerSymbol);
		// then
		assertThat(response).extracting("tickerSymbol").isEqualTo(tickerSymbol);
		assertThat(response).extracting("currentPrice").isNotNull();
	}

	@DisplayName("별도의 쓰레드로 실행시 AccessTokenAspect이 수행하여 새로운 엑세스 토큰을 갱신한다")
	@Test
	void readRealTimeCurrentPriceWithAspectAndRunnable() throws
		ExecutionException,
		InterruptedException,
		TimeoutException {
		// given
		String tickerSymbol = "005930";
		// when
		CompletableFuture<CurrentPriceResponse> future = CompletableFuture.supplyAsync(
			() -> kisService.readRealTimeCurrentPrice(tickerSymbol));

		CurrentPriceResponse response = future.get(2L, TimeUnit.SECONDS);
		// then
		assertThat(response).extracting("tickerSymbol").isEqualTo(tickerSymbol);
		assertThat(response).extracting("currentPrice").isNotNull();
	}

}
