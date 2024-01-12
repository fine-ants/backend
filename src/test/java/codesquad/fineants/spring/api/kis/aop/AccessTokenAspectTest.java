package codesquad.fineants.spring.api.kis.aop;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.spring.api.kis.client.KisAccessToken;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.properties.OauthKisProperties;
import codesquad.fineants.spring.api.kis.service.KisRedisService;
import reactor.core.publisher.Mono;

@ActiveProfiles("test")
@SpringBootTest
class AccessTokenAspectTest {

	@MockBean
	private KisClient client;

	@Autowired
	private KisRedisService kisRedisService;

	@Autowired
	private OauthKisProperties oauthKisProperties;

	@AfterEach
	void tearDown() {
		kisRedisService.deleteAccessTokenMap();
	}

	@DisplayName("액세스 토큰을 새로 발급하여 redis에 저장한다")
	@Test
	void checkAccessTokenExpiration() {
		// given
		AccessTokenAspect accessTokenAspect = new AccessTokenAspect(new KisAccessTokenManager(null), client,
			kisRedisService, oauthKisProperties);
		kisRedisService.deleteAccessTokenMap();

		given(client.accessToken(anyString()))
			.willReturn(
				Mono.just(createKisAccessToken())
					.delayElement(Duration.ofMillis(500L))
			);
		// when
		accessTokenAspect.checkAccessTokenExpiration();
		// then
		Assertions.assertThat(kisRedisService.getAccessTokenMap().isPresent()).isTrue();
	}

	private KisAccessToken createKisAccessToken() {
		return new KisAccessToken(
			"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6ImE1OGY4YzAyLWMzMzYtNGY3ZC04OGE0LWZkZDRhZTA3NmQ5YyIsImlzcyI6InVub2d3IiwiZXhwIjoxNzAxOTE2ODg3LCJpYXQiOjE3MDE4MzA0ODcsImp0aSI6IlBTRGc4WlVJd041eVl5ZkR6bnA0TDM2Z2xhRUpic2RJNGd6biJ9.uLZAu9_ompf8ycwiRJ5jrdoB-MiUG9a8quoQ3OeVOrUDGxyEhHmzZTPnCdLRWOEHowFlmyNOf3v-lPZGZqi9Kw",
			"Bearer",
			LocalDateTime.now().plusDays(1),
			86400
		);
	}
}