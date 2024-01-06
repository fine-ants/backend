package codesquad.fineants.spring.api.kis.client;

import static java.nio.charset.StandardCharsets.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import codesquad.fineants.spring.util.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class KisClientTest {

	@Autowired
	private KisClient kisClient;

	@MockBean
	private WebClient webClient;

	@DisplayName("한국투자증권 서버로부터 액세스 토큰 발급이 한번 실패하는 경우 재발급을 다시 요청한다")
	@Test
	void accessToken() {
		// given
		WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
		WebClient.RequestHeadersSpec requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
		WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

		KisAccessToken accessToken = createKisAccessToken();
		when(webClient.post()).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.bodyValue(anyMap())).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

		when(responseSpec.onStatus(any(), any()))
			.thenReturn(responseSpec);
		when(responseSpec.bodyToMono(KisAccessToken.class))
			.thenReturn(Mono.error(createError()));
		when(responseSpec.bodyToMono(KisAccessToken.class)
			.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofMinutes(1L)))
		).thenReturn(Mono.just(accessToken));

		// when & then
		Mono<KisAccessToken> mono = kisClient.accessToken();
		StepVerifier.create(mono)
			.expectNext(accessToken)
			.verifyComplete();
	}

	private WebClientResponseException createError() {
		Map<String, String> responseBody = new HashMap<>();
		responseBody.put("error_description", "접근토큰 발급 잠시 후 다시 시도하세요(1분당 1회)");
		responseBody.put("error_code", "EGW00133");
		return new WebClientResponseException("Forbidden", HttpStatus.FORBIDDEN.value(),
			"Forbidden", null, ObjectMapperUtil.serialize(responseBody).getBytes(UTF_8),
			UTF_8);
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
