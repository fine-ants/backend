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
		String idToken = "eyJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJkZmIxZTI1YTJiOTdkMDNiMGIyMjVkNDg3NGEzNDgyMyIsInN1YiI6IjMxNjA5OTI1NjMiLCJhdXRoX3RpbWUiOjE2OTk4NjY2MjIsImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwiZXhwIjoxNjk5ODg4MjIyLCJpYXQiOjE2OTk4NjY2MjIsIm5vbmNlIjoiZjQ2ZTI5NzczNzhlZDZjZGYyZjAzYjU5NjIxMDFmN2QiLCJwaWN0dXJlIjoiaHR0cDovL2sua2FrYW9jZG4ubmV0L2RuL2RwazlsMS9idHFtR2hBMmxLTC9PejB3RHVKbjFZVjJESW45MmY2RFZLL2ltZ18xMTB4MTEwLmpwZyIsImVtYWlsIjoiZmluZWFudHMuY29AZ21haWwuY29tIn0.KYRaqSup_joMxGSYoWSa5bEvQRbCR3q_kUydXaDs_otD1fmJFWW3h8UsrKaHyaDT6mMIobeuTAXlhjiY7rxqhr3K3cqj3urZThIJ-h4-5VrANC6aulzyhJB93kcIJUxa2UzI0fe_TyC4pbbGvRnFDci0Iv5CuVZoL0-oLgu5LQFpM1xmNmF_SABKuIU0rV60E_Qs7PsbnodZ_emv4PLEtk1yUD1r5oAr9swtSjLG_uh1d2GXUvcIStIJN871Kp-jolqM1Ce4bp0zULd00x0nGKbPBHmS_J11NYJbjujeg771307w65KCYIPMMqM_1BbEFQghH3jDRdzwP_5_RRKhAg";
		String nonce = "f46e2977378ed6cdf2f03b5962101f7d";
		// when
		DecodedIdTokenPayload payload = oauthClient.decodeIdToken(idToken, nonce, LocalDateTime.now());
		// then
		assertThat(payload.getEmail()).isEqualTo("fineants.co@gmail.com");
	}

	@DisplayName("IdToken을 디코딩할때 유효하지 않은 토큰으로 디코딩할 수 없다")
	@Test
	void decodeIdTokenWithInvalidIdToken() {
		// given
		OauthClient oauthClient = oauthClientRepository.findOneBy("kakao");
		String idToken = "yJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJkZmIxZTI1YTJiOTdkMDNiMGIyMjVkNDg3NGEzNDgyMyIsInN1YiI6IjMxNjA5OTI1NjMiLCJhdXRoX3RpbWUiOjE2OTk4NjY2MjIsImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwiZXhwIjoxNjk5ODg4MjIyLCJpYXQiOjE2OTk4NjY2MjIsIm5vbmNlIjoiZjQ2ZTI5NzczNzhlZDZjZGYyZjAzYjU5NjIxMDFmN2QiLCJwaWN0dXJlIjoiaHR0cDovL2sua2FrYW9jZG4ubmV0L2RuL2RwazlsMS9idHFtR2hBMmxLTC9PejB3RHVKbjFZVjJESW45MmY2RFZLL2ltZ18xMTB4MTEwLmpwZyIsImVtYWlsIjoiZmluZWFudHMuY29AZ21haWwuY29tIn0.KYRaqSup_joMxGSYoWSa5bEvQRbCR3q_kUydXaDs_otD1fmJFWW3h8UsrKaHyaDT6mMIobeuTAXlhjiY7rxqhr3K3cqj3urZThIJ-h4-5VrANC6aulzyhJB93kcIJUxa2UzI0fe_TyC4pbbGvRnFDci0Iv5CuVZoL0-oLgu5LQFpM1xmNmF_SABKuIU0rV60E_Qs7PsbnodZ_emv4PLEtk1yUD1r5oAr9swtSjLG_uh1d2GXUvcIStIJN871Kp-jolqM1Ce4bp0zULd00x0nGKbPBHmS_J11NYJbjujeg771307w65KCYIPMMqM_1BbEFQghH3jDRdzwP_5_RRKhAg";
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
		String idToken = "eyJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJkZmIxZTI1YTJiOTdkMDNiMGIyMjVkNDg3NGEzNDgyMyIsInN1YiI6IjMxNjA5OTI1NjMiLCJhdXRoX3RpbWUiOjE2OTk4NjY2MjIsImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwiZXhwIjoxNjk5ODg4MjIyLCJpYXQiOjE2OTk4NjY2MjIsIm5vbmNlIjoiZjQ2ZTI5NzczNzhlZDZjZGYyZjAzYjU5NjIxMDFmN2QiLCJwaWN0dXJlIjoiaHR0cDovL2sua2FrYW9jZG4ubmV0L2RuL2RwazlsMS9idHFtR2hBMmxLTC9PejB3RHVKbjFZVjJESW45MmY2RFZLL2ltZ18xMTB4MTEwLmpwZyIsImVtYWlsIjoiZmluZWFudHMuY29AZ21haWwuY29tIn0.KYRaqSup_joMxGSYoWSa5bEvQRbCR3q_kUydXaDs_otD1fmJFWW3h8UsrKaHyaDT6mMIobeuTAXlhjiY7rxqhr3K3cqj3urZThIJ-h4-5VrANC6aulzyhJB93kcIJUxa2UzI0fe_TyC4pbbGvRnFDci0Iv5CuVZoL0-oLgu5LQFpM1xmNmF_SABKuIU0rV60E_Qs7PsbnodZ_emv4PLEtk1yUD1r5oAr9swtSjLG_uh1d2GXUvcIStIJN871Kp-jolqM1Ce4bp0zULd00x0nGKbPBHmS_J11NYJbjujeg771307w65KCYIPMMqM_1BbEFQghH3jDRdzwP_5_RRKhAg";
		String nonce = "1234";
		// when
		Throwable throwable = catchThrowable(() -> oauthClient.decodeIdToken(idToken, nonce, LocalDateTime.now()));
		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("nonce 값 f46e2977378ed6cdf2f03b5962101f7d과 일치하지 않습니다. nonce=1234");
	}

}
