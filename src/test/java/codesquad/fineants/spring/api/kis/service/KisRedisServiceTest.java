package codesquad.fineants.spring.api.kis.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.spring.api.kis.client.KisAccessToken;

@ActiveProfiles("test")
@SpringBootTest
class KisRedisServiceTest {

	@Autowired
	private KisRedisService service;

	@AfterEach
	void tearDown() {
		service.deleteAccessTokenMap();
	}

	@DisplayName("kis 액세스 토큰맵을 저장한다")
	@Test
	void setAccessTokenMap() {
		// given
		KisAccessToken kisAccessToken = createKisAccessToken();

		// when
		service.setAccessTokenMap(kisAccessToken, createNow());

		// then
		assertThat(service.getAccessTokenMap().isPresent()).isTrue();
	}

	@DisplayName("이미 만료된 액세스 토큰을 redis에 저장할 수 없다.")
	@Test
	void setAccessTokenMapWithExpiredAccessToken() {
		// given
		KisAccessToken accessToken = createKisAccessToken();
		// when
		Throwable throwable = catchThrowable(
			() -> service.setAccessTokenMap(accessToken, LocalDateTime.of(2023, 12, 8, 15, 0, 0)));

		// then
		assertThat(throwable)
			.isInstanceOf(RuntimeException.class);
	}

	@DisplayName("kis 액세스 토큰맵을 가져온다")
	@Test
	void getAccessTokenMap() {
		// given
		service.setAccessTokenMap(createKisAccessToken(), createNow());

		// when
		KisAccessToken accessToken = service.getAccessTokenMap().orElseThrow();

		// then
		assertThat(accessToken)
			.extracting("accessToken", "tokenType", "accessTokenExpired", "expiresIn")
			.containsExactlyInAnyOrder(
				"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6ImE1OGY4YzAyLWMzMzYtNGY3ZC04OGE0LWZkZDRhZTA3NmQ5YyIsImlzcyI6InVub2d3IiwiZXhwIjoxNzAxOTE2ODg3LCJpYXQiOjE3MDE4MzA0ODcsImp0aSI6IlBTRGc4WlVJd041eVl5ZkR6bnA0TDM2Z2xhRUpic2RJNGd6biJ9.uLZAu9_ompf8ycwiRJ5jrdoB-MiUG9a8quoQ3OeVOrUDGxyEhHmzZTPnCdLRWOEHowFlmyNOf3v-lPZGZqi9Kw",
				"Bearer",
				LocalDateTime.of(2023, 12, 7, 11, 41, 27),
				86400
			);
	}

	@DisplayName("redis에 accessToken이 없는 경우 Optional.empty()를 반환한다")
	@Test
	void getAccessTokenMap_whenEmptyAccessToken_thenReturnEmptyOptional() {
		// given
		service.deleteAccessTokenMap();
		// when
		Optional<KisAccessToken> optionalKisAccessToken = service.getAccessTokenMap();
		// then
		assertThat(optionalKisAccessToken.isEmpty()).isTrue();
	}

	private LocalDateTime createNow() {
		return LocalDateTime.of(2023, 12, 6, 14, 0, 0);
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
