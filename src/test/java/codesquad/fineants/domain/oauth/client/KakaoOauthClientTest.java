package codesquad.fineants.domain.oauth.client;

import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class KakaoOauthClientTest {

	@Autowired
	private OauthClientRepository oauthClientRepository;

	@Autowired
	private OauthClientRandomGenerator oauthClientRandomGenerator;

	@DisplayName("인가코드 URL을 생성한다")
	@Test
	void createAuthURL() {
		// given
		OauthClient oauthClient = oauthClientRepository.findOneBy("kakao");
		String state = oauthClientRandomGenerator.generateState();
		String codeVerifier = oauthClientRandomGenerator.generateCodeVerifier();
		String nonce = oauthClientRandomGenerator.generateNonce();
		String codeChallenge = oauthClientRandomGenerator.generateCodeChallenge(codeVerifier);
		// when
		String actual = oauthClient.createAuthURL(state, codeChallenge, nonce);

		// then
		String expected = "https://kauth.kakao.com/oauth/authorize?"
			+ "response_type=code"
			+ "&client_id=88171956c99257e9ea8c241b4fd544df"
			+ "&redirect_uri=https://localhost/api/auth/kakao/login"
			+ "&scope=openid"
			+ "&state=" + state
			+ "&nonce=" + nonce
			+ "&code_challenge=" + codeChallenge
			+ "&code_challenge_method=S256";
		log.info("actual={}", actual);
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	@DisplayName("idToken의 유효성을 검증한다")
	@Test
	void validateIdToken() {
		// given
		OauthClient oauthClient = oauthClientRepository.findOneBy("kakao");
		String nonce = "5b9a1e56120de83c4a083894c8dc8127";
		OauthPublicKey[] publicKeys = new OauthPublicKey[] {
			new OauthPublicKey("3f96980381e451efad0d2ddd30e3d3", "RSA", "RS256", "sig",
				"q8zZ0b_MNaLd6Ny8wd4cjFomilLfFIZcmhNSc1ttx_oQdJJZt5CDHB8WWwPGBUDUyY8AmfglS9Y1qA0_fxxs", "AQAB"),
			new OauthPublicKey("9f252dadd5f233f93d2fa528d12fea", "RSA", "RS256", "sig",
				"qGWf6RVzV2pM8YqJ6by5exoixIlTvdXDfYj2v7E6xkoYmesAjp_1IYL7rzhpUYqIkWX0P4wOwAsg", "AQAB")
		};
		// when
		oauthClient.validateIdToken(
			"eyJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI4ODE3MTk1NmM5OTI1N2U5ZWE4YzI0MWI0ZmQ1NDRkZiIsInN1YiI6IjMxMDE1NDMzNjUiLCJhdXRoX3RpbWUiOjE2OTk1MDg3MTEsImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwiZXhwIjoxNjk5NTMwMzExLCJpYXQiOjE2OTk1MDg3MTEsIm5vbmNlIjoiNWI5YTFlNTYxMjBkZTgzYzRhMDgzODk0YzhkYzgxMjciLCJwaWN0dXJlIjoiaHR0cDovL2sua2FrYW9jZG4ubmV0L2RuL2RwazlsMS9idHFtR2hBMmxLTC9PejB3RHVKbjFZVjJESW45MmY2RFZLL2ltZ18xMTB4MTEwLmpwZyIsImVtYWlsIjoicWtkbGZqdG0xMTlAbmF2ZXIuY29tIn0.H32Q0NE4Wcy-XWoIKXRXEXhjx3z9kpB5Wfppkm3V_8yAN8HgJzj7RT4zM7_xeHNAdf5grFHm5CpE-UpZKAWMKA1aikAMmyuIKBvcjWTIE97pHS0HGP-vPY6Kp6l-ZYj6aAlafJq93gcGba3kE-9oha8N48aHyv6G7GapZAQghjRNK2az-2YURcaNDkXXCH3Gnntnx-lZ-NKnTgEeAsD4yvU9rW39wlit7rHE4uhXGBsqaxglTK6WogxOhA89_aZsHTGZdn2BkIjRseafzBrGuk0ltDQxc1TCakrjK4bUCXB1q7yYwm5y-zbqb1iUNWd70-mwU3ko1YDUVbLBi0EuAA",
			nonce, Arrays.asList(publicKeys));
		// then

	}
}
