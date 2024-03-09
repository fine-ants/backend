package codesquad.fineants.domain.oauth.decoder;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.spring.api.common.errors.exception.BadRequestException;

@ActiveProfiles("test")
class KakaoIDTokenDecoderTest {

	@DisplayName("IdToken을 디코딩할때 유효하지 않은 토큰으로 디코딩할 수 없다")
	@Test
	void decodeIdTokenWithInvalidIdToken() {
		// given
		IDTokenDecoder decoder = new KakaoIDTokenDecoder();
		String idToken = "aweiofjawioe.awoiefjaoweji.aoiwejfoajwe";
		String jwkUri = "https://kauth.kakao.com/.well-known/jwks.json";

		// when
		Throwable throwable = catchThrowable(() -> decoder.decode(idToken, jwkUri));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("유효하지 않은 ID Token입니다");
	}
}
