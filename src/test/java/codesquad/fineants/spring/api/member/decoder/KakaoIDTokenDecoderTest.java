package codesquad.fineants.spring.api.member.decoder;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.spring.api.errors.exception.BadRequestException;

@ActiveProfiles("test")
class KakaoIDTokenDecoderTest {

	@DisplayName("IdToken을 디코딩할때 유효하지 않은 토큰으로 디코딩할 수 없다")
	@Test
	void decodeIdTokenWithInvalidIdToken() {
		// given
		IDTokenDecoder decoder = new KakaoIDTokenDecoder();
		String idToken = "yJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJkZmIxZTI1YTJiOTdkMDNiMGIyMjVkNDg3NGEzNDgyMyIsInN1YiI6IjMxNjA5OTI1NjMiLCJhdXRoX3RpbWUiOjE2OTk4NjY2MjIsImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwiZXhwIjoxNjk5ODg4MjIyLCJpYXQiOjE2OTk4NjY2MjIsIm5vbmNlIjoiZjQ2ZTI5NzczNzhlZDZjZGYyZjAzYjU5NjIxMDFmN2QiLCJwaWN0dXJlIjoiaHR0cDovL2sua2FrYW9jZG4ubmV0L2RuL2RwazlsMS9idHFtR2hBMmxLTC9PejB3RHVKbjFZVjJESW45MmY2RFZLL2ltZ18xMTB4MTEwLmpwZyIsImVtYWlsIjoiZmluZWFudHMuY29AZ21haWwuY29tIn0.KYRaqSup_joMxGSYoWSa5bEvQRbCR3q_kUydXaDs_otD1fmJFWW3h8UsrKaHyaDT6mMIobeuTAXlhjiY7rxqhr3K3cqj3urZThIJ-h4-5VrANC6aulzyhJB93kcIJUxa2UzI0fe_TyC4pbbGvRnFDci0Iv5CuVZoL0-oLgu5LQFpM1xmNmF_SABKuIU0rV60E_Qs7PsbnodZ_emv4PLEtk1yUD1r5oAr9swtSjLG_uh1d2GXUvcIStIJN871Kp-jolqM1Ce4bp0zULd00x0nGKbPBHmS_J11NYJbjujeg771307w65KCYIPMMqM_1BbEFQghH3jDRdzwP_5_RRKhAg";
		String jwkUri = "https://kauth.kakao.com/.well-known/jwks.json";

		// when
		Throwable throwable = catchThrowable(() -> decoder.decode(idToken, jwkUri));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("유효하지 않은 ID Token입니다");
	}
}
