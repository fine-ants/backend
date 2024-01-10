package codesquad.fineants.spring.api.kis.client;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import codesquad.fineants.spring.api.kis.properties.OauthKisProperties;
import codesquad.fineants.spring.api.kis.service.KisService;
import codesquad.fineants.spring.util.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class KisClientTest {
	public static MockWebServer mockWebServer;

	@Autowired
	private OauthKisProperties oauthKisProperties;

	private KisClient kisClient;

	@MockBean
	private KisService kisService; // 스케줄링 메소드 비활성화

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
		String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
		this.kisClient = new KisClient(
			oauthKisProperties,
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
		Mono<KisAccessToken> responseMono = this.kisClient.accessToken("/oauth2/tokenP");

		// then
		StepVerifier
			.withVirtualTime(() -> responseMono)
			.expectSubscription()
			.thenAwait(Duration.ofMinutes(1))
			.expectNextMatches(expectedKisAccessToken::equals)
			.expectComplete()
			.verify();
	}

	private Map<String, String> createError() {
		Map<String, String> responseBody = new HashMap<>();
		responseBody.put("error_description", "접근토큰 발급 잠시 후 다시 시도하세요(1분당 1회)");
		responseBody.put("error_code", "EGW00133");
		return responseBody;
	}

	private KisAccessToken createKisAccessToken() {
		return new KisAccessToken(
			"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6ImE1OGY4YzAyLWMzMzYtNGY3ZC04OGE0LWZkZDRhZTA3NmQ5YyIsImlzcyI6InVub2d3IiwiZXhwIjoxNzAxOTE2ODg3LCJpYXQiOjE3MDE4MzA0ODcsImp0aSI6IlBTRGc4WlVJd041eVl5ZkR6bnA0TDM2Z2xhRUpic2RJNGd6biJ9.uLZAu9_ompf8ycwiRJ5jrdoB-MiUG9a8quoQ3OeVOrUDGxyEhHmzZTPnCdLRWOEHowFlmyNOf3v-lPZGZqi9Kw",
			"Bearer",
			LocalDateTime.of(2023, 12, 7, 11, 41, 27),
			86400
		);
	}
}

