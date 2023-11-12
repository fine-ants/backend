package codesquad.fineants.domain.oauth.client;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;

@ActiveProfiles("test")
@SpringBootTest
class OauthClientTest {

	@Autowired
	private OauthClientRepository oauthClientRepository;

	@DisplayName("IdToken을 디코딩하여 페이로드를 가져온다")
	@Test
	void decodeIdToken() {
		// given
		OauthClient oauthClient = oauthClientRepository.findOneBy("kakao");
		String idToken = "eyJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI4ODE3MTk1NmM5OTI1N2U5ZWE4YzI0MWI0ZmQ1NDRkZiIsInN1YiI6IjMxMDE1NDMzNjUiLCJhdXRoX3RpbWUiOjE2OTk3NTg0MzAsImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwiZXhwIjoxNjk5NzgwMDMwLCJpYXQiOjE2OTk3NTg0MzAsIm5vbmNlIjoiZDE3ZGQ3YjBlOTIxOWNkY2U4MTg5ZmIzMWQ4NTg1NjgiLCJwaWN0dXJlIjoiaHR0cDovL2sua2FrYW9jZG4ubmV0L2RuL2RwazlsMS9idHFtR2hBMmxLTC9PejB3RHVKbjFZVjJESW45MmY2RFZLL2ltZ18xMTB4MTEwLmpwZyIsImVtYWlsIjoicWtkbGZqdG0xMTlAbmF2ZXIuY29tIn0.kz7R4T4dqyGpfZLmWSZzwsDPEgQyMoOzqpDXJofkwOIR0s7x8dUI43wPo-rqDe9xpoi7zadH-FkijGNzvBlyMa5jlao_MrftXACITj2jkDiVaDmnxw6VumaWfNivcnYCHbENmPU7i_V0-a1RFVBfk8_zH1UdkI5LQokQX0y9FvNhXyuLl1u5KQfh5ocWzPwpnI8XeOlzkL78k9ZWysTZLpwW0H-4qx_81TQXPa1AjFooBuxjsyxPgXWqSOohYsCaWK2jnDwk13DM1zop4ElmMKM6eC2M_xIIFPoBu41KRvAah7Tiqn7pLQPwecYNQ4RVR-Ud66xtYiNxyTFGHBg0mA";
		String nonce = "d17dd7b0e9219cdce8189fb31d858568";
		// when
		DecodedIdTokenPayload payload = oauthClient.decodeIdToken(idToken, nonce, LocalDateTime.now());
		// then
		assertThat(payload.getEmail()).isEqualTo("qkdlfjtm119@naver.com");
	}

	@DisplayName("IdToken을 디코딩할때 유효하지 않은 토큰으로 디코딩할 수 없다")
	@Test
	void decodeIdTokenWithInvalidIdToken() {
		// given
		OauthClient oauthClient = oauthClientRepository.findOneBy("kakao");
		String idToken = "yJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI4ODE3MTk1NmM5OTI1N2U5ZWE4YzI0MWI0ZmQ1NDRkZiIsInN1YiI6IjMxMDE1NDMzNjUiLCJhdXRoX3RpbWUiOjE2OTk3NTg0MzAsImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwiZXhwIjoxNjk5NzgwMDMwLCJpYXQiOjE2OTk3NTg0MzAsIm5vbmNlIjoiZDE3ZGQ3YjBlOTIxOWNkY2U4MTg5ZmIzMWQ4NTg1NjgiLCJwaWN0dXJlIjoiaHR0cDovL2sua2FrYW9jZG4ubmV0L2RuL2RwazlsMS9idHFtR2hBMmxLTC9PejB3RHVKbjFZVjJESW45MmY2RFZLL2ltZ18xMTB4MTEwLmpwZyIsImVtYWlsIjoicWtkbGZqdG0xMTlAbmF2ZXIuY29tIn0.kz7R4T4dqyGpfZLmWSZzwsDPEgQyMoOzqpDXJofkwOIR0s7x8dUI43wPo-rqDe9xpoi7zadH-FkijGNzvBlyMa5jlao_MrftXACITj2jkDiVaDmnxw6VumaWfNivcnYCHbENmPU7i_V0-a1RFVBfk8_zH1UdkI5LQokQX0y9FvNhXyuLl1u5KQfh5ocWzPwpnI8XeOlzkL78k9ZWysTZLpwW0H-4qx_81TQXPa1AjFooBuxjsyxPgXWqSOohYsCaWK2jnDwk13DM1zop4ElmMKM6eC2M_xIIFPoBu41KRvAah7Tiqn7pLQPwecYNQ4RVR-Ud66xtYiNxyTFGHBg0mA";
		String nonce = "d17dd7b0e9219cdce8189fb31d858568";
		// when
		Throwable throwable = catchThrowable(() -> oauthClient.decodeIdToken(idToken, nonce, LocalDateTime.now()));
		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("유효하지 않은 ID Token입니다");
	}

	@DisplayName("IdToken을 디코딩할때 유효하지 않은 nonce로 디코딩할 수 없다")
	@Test
	void decodeIdTokenWithInvalidNonce() {
		// given
		OauthClient oauthClient = oauthClientRepository.findOneBy("kakao");
		String idToken = "eyJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI4ODE3MTk1NmM5OTI1N2U5ZWE4YzI0MWI0ZmQ1NDRkZiIsInN1YiI6IjMxMDE1NDMzNjUiLCJhdXRoX3RpbWUiOjE2OTk3NTg0MzAsImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwiZXhwIjoxNjk5NzgwMDMwLCJpYXQiOjE2OTk3NTg0MzAsIm5vbmNlIjoiZDE3ZGQ3YjBlOTIxOWNkY2U4MTg5ZmIzMWQ4NTg1NjgiLCJwaWN0dXJlIjoiaHR0cDovL2sua2FrYW9jZG4ubmV0L2RuL2RwazlsMS9idHFtR2hBMmxLTC9PejB3RHVKbjFZVjJESW45MmY2RFZLL2ltZ18xMTB4MTEwLmpwZyIsImVtYWlsIjoicWtkbGZqdG0xMTlAbmF2ZXIuY29tIn0.kz7R4T4dqyGpfZLmWSZzwsDPEgQyMoOzqpDXJofkwOIR0s7x8dUI43wPo-rqDe9xpoi7zadH-FkijGNzvBlyMa5jlao_MrftXACITj2jkDiVaDmnxw6VumaWfNivcnYCHbENmPU7i_V0-a1RFVBfk8_zH1UdkI5LQokQX0y9FvNhXyuLl1u5KQfh5ocWzPwpnI8XeOlzkL78k9ZWysTZLpwW0H-4qx_81TQXPa1AjFooBuxjsyxPgXWqSOohYsCaWK2jnDwk13DM1zop4ElmMKM6eC2M_xIIFPoBu41KRvAah7Tiqn7pLQPwecYNQ4RVR-Ud66xtYiNxyTFGHBg0mA";
		String nonce = "1234";
		// when
		Throwable throwable = catchThrowable(() -> oauthClient.decodeIdToken(idToken, nonce, LocalDateTime.now()));
		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("nonce 값 d17dd7b0e9219cdce8189fb31d858568과 일치하지 않습니다. nonce=1234");
	}

}
