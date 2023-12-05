package codesquad.fineants.domain.oauth.client;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.oauth.client.kakao.KakaoDecodedIdTokenPayload;
import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;

@ActiveProfiles("test")
@SpringBootTest
class OauthClientTest {

	@Autowired
	private OauthClientRepository oauthClientRepository;

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

	@DisplayName("Payload를 검증할 때 유효하지 않은 nonce을 검증한다")
	@Test
	void validatePayloadWithInvalidNonce() throws JsonProcessingException {
		// given
		OauthClient oauthClient = oauthClientRepository.findOneBy("kakao");
		String nonce = "1234";
		long exp = LocalDateTime.now().plusMinutes(5L).toEpochSecond(ZoneOffset.UTC);
		DecodedIdTokenPayload payload = createDecodedIdTokenPayload(exp);

		// when
		Throwable throwable = catchThrowable(() -> oauthClient.validatePayload(payload, LocalDateTime.now(), nonce));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("nonce 값 f46e2977378ed6cdf2f03b5962101f7d과 일치하지 않습니다. nonce=1234");
	}

	private static DecodedIdTokenPayload createDecodedIdTokenPayload(long second) throws JsonProcessingException {
		Map<String, Object> payloadMap = new HashMap<>();
		payloadMap.put("iss", "https://kauth.kakao.com");
		payloadMap.put("aud", "dfb1e25a2b97d03b0b225d4874a34823");
		payloadMap.put("sub", "3160992563");
		payloadMap.put("auth_time", second);
		payloadMap.put("iat", second);
		payloadMap.put("exp", second);
		payloadMap.put("nonce", "f46e2977378ed6cdf2f03b5962101f7d");
		payloadMap.put("picture", "http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_110x110.jpg");
		payloadMap.put("email", "fineants.co@gmail.com");

		ObjectMapper objectMapper = new ObjectMapper();
		DecodedIdTokenPayload payload = objectMapper.readValue(objectMapper.writeValueAsString(payloadMap),
			KakaoDecodedIdTokenPayload.class);
		return payload;
	}
}
