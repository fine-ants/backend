package codesquad.fineants.domain.oauth.client;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.oauth.client.kakao.KakaoDecodedIdTokenPayload;
import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.common.errors.exception.BadRequestException;

class OauthClientTest extends AbstractContainerBaseTest {

	@Autowired
	private OauthClientRepository oauthClientRepository;

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
