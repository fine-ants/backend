package codesquad.fineants.spring.api.kis;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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

	@Autowired
	private CurrentPriceManager manager;

	@MockBean
	private KisClient client;

	@MockBean
	private KisAccessTokenManager kisAccessTokenManager;

	@Autowired
	private KisRedisService kisRedisService;

	@DisplayName("주식 현재가 시세를 가져온다")
	@Test
	void readRealTimeCurrentPrice() {
		// given
		String tickerSymbol = "005930";
		given(kisAccessTokenManager.createAuthorization()).willReturn(
			"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6Ijg5MjBlNjM2LTNkYmItNGU5MS04ZGJmLWJmZDU5ZmI2YjAwYiIsImlzcyI6InVub2d3IiwiZXhwIjoxNzAzNTcwOTA0LCJpYXQiOjE3MDM0ODQ1MDQsImp0aSI6IlBTRGc4WlVJd041eVl5ZkR6bnA0TDM2Z2xhRUpic2RJNGd6biJ9.z8dh9rlOyPq_ukm9KeCz0tkKI2QaHEe07LhXTcKQBrcP1-uiW3dwAwdknpAojJZ7aUWLUaQQn0HmjTCttjSJaA");
		given(client.readRealTimeCurrentPrice(anyString(), anyString())).willReturn(60000L);
		// when
		CurrentPriceResponse response = kisService.readRealTimeCurrentPrice(tickerSymbol);
		// then
		assertThat(response)
			.extracting("tickerSymbol", "currentPrice")
			.containsExactlyInAnyOrder("005930", 60000L);
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

	@DisplayName("현재가 및 종가 갱신 전에 액세스 토큰을 새로 발급받아 갱신한다")
	@Test
	void refreshStockPrice() {
		// given
		kisRedisService.deleteAccessTokenMap();
		given(kisAccessTokenManager.isAccessTokenExpired(ArgumentMatchers.any(LocalDateTime.class)))
			.willReturn(true);
		given(client.accessToken()).willReturn(createAccessTokenMap(LocalDateTime.now()));
		// when
		kisService.refreshStockPrice();
		// then
		assertThat(kisRedisService.getAccessTokenMap()).isNotNull();
	}

	private Map<String, Object> createAccessTokenMap(LocalDateTime now) {
		Map<String, Object> map = new HashMap<>();
		map.put("access_token",
			"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6IjBiYWFlYzg1LWU0YjctNDFlOS05ODk1LTUyNDE2ODRjNDhkOSIsImlzcyI6InVub2d3IiwiZXhwIjoxNzAzNDcxMzg2LCJpYXQiOjE3MDMzODQ5ODYsImp0aSI6IlBTRGc4WlVJd041eVl5ZkR6bnA0TDM2Z2xhRUpic2RJNGd6biJ9.mrJht_O2aRrhSPN1DSmHKarwAfgDpr4GECvF30Is2EI0W6ypbe7DXwXmluhQXT0h1g7OHhGhyBhDNtya4LcctQ");
		map.put("access_token_token_expired",
			now.plusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		map.put("token_type", "Bearer");
		map.put("expires_in", 86400);
		return map;
	}

}
