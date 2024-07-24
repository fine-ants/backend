package codesquad.fineants.domain.kis.client;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.kis.domain.dto.response.KisIPOResponse;
import codesquad.fineants.domain.kis.domain.dto.response.KisIpo;
import codesquad.fineants.domain.kis.properties.OauthKisProperties;
import codesquad.fineants.global.util.ObjectMapperUtil;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class KisClientTest extends AbstractContainerBaseTest {
	public static MockWebServer mockWebServer;

	private KisClient kisClient;

	@BeforeAll
	static void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
	}

	@AfterAll
	static void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@BeforeEach
	void initialize() {
		OauthKisProperties oauthKisProperties = new OauthKisProperties(
			"appkey",
			"secertkey",
			"otkenURI",
			"currentPriceURI",
			"lastDayClosingPriceURI",
			"dividendURI",
			"ipoURI",
			"searchStockInfoURI"
		);
		String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
		this.kisClient = new KisClient(
			oauthKisProperties,
			WebClient.builder().baseUrl(baseUrl).build(),
			WebClient.builder().baseUrl(baseUrl).build());
	}

	@DisplayName("한국투자증권 서버로부터 액세스 토큰 발급이 한번 실패하는 경우 재발급을 다시 요청한다")
	@Test
	void accessToken_whenIssueAccessToken_thenRetryOnAccessTokenFailure() {
		// given
		KisAccessToken expectedKisAccessToken = createKisAccessToken();
		mockWebServer.enqueue(new MockResponse().setResponseCode(403)
			.setBody(ObjectMapperUtil.serialize(createError()))
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));
		mockWebServer.enqueue(new MockResponse().setResponseCode(200)
			.setBody(ObjectMapperUtil.serialize(expectedKisAccessToken))
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

		// when
		Mono<KisAccessToken> responseMono = this.kisClient.fetchAccessToken();

		// then
		StepVerifier
			.withVirtualTime(() -> responseMono)
			.expectSubscription()
			.thenAwait(Duration.ofMinutes(1))
			.expectNextMatches(expectedKisAccessToken::equals)
			.expectComplete()
			.verify();
	}

	@DisplayName("어제부터 오늘까지 상장된 종목들을 조회한다")
	@Test
	void fetchIpo() {
		// given
		Map<String, Object> responseBodyMap = new HashMap<>();
		List<Map<String, String>> output1 = new ArrayList<>();
		Map<String, String> stock1 = new HashMap<>();
		stock1.put("list_dt", "20240326");
		stock1.put("sht_cd", "034220");
		stock1.put("isin_name", "LG디스플레이");
		stock1.put("stk_kind", "보통");
		stock1.put("issue_type", "유상증자");
		stock1.put("issue_stk_qty", "142184300");
		stock1.put("tot_issue_stk_qty", "500000000");
		stock1.put("issue_price", "9090");

		output1.add(stock1);
		responseBodyMap.put("output1", output1);
		mockWebServer.enqueue(new MockResponse().setResponseCode(200)
			.setBody(ObjectMapperUtil.serialize(responseBodyMap))
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));

		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		KisAccessToken kisAccessToken = createKisAccessToken();
		// when
		KisIPOResponse response = kisClient.fetchIpo(yesterday, today, kisAccessToken.createAuthorization());

		// then
		assertAll(
			() -> assertThat(response).isNotNull(),
			() -> assertThat(Objects.requireNonNull(response).getDatas())
				.hasSize(1)
				.extracting(KisIpo::getListDt, KisIpo::getShtCd, KisIpo::getIsinName)
				.containsExactlyInAnyOrder(Tuple.tuple("20240326", "034220", "LG디스플레이"))
		);
	}

	private Map<String, String> createError() {
		Map<String, String> responseBody = new HashMap<>();
		responseBody.put("error_description", "접근토큰 발급 잠시 후 다시 시도하세요(1분당 1회)");
		responseBody.put("error_code", "EGW00133");
		return responseBody;
	}

	public KisAccessToken createKisAccessToken() {
		return new KisAccessToken(
			"accessToken",
			"Bearer",
			LocalDateTime.of(2023, 12, 7, 11, 41, 27),
			86400
		);
	}
}

