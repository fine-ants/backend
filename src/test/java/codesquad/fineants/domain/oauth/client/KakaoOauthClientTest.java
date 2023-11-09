package codesquad.fineants.domain.oauth.client;

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

	@DisplayName("인가코드 URL을 생성한다")
	@Test
	void createAuthURL() {
		// given
		OauthClient oauthClient = oauthClientRepository.findOneBy("kakao");
		String state = oauthClient.generateState();
		String codeVerifier = oauthClient.generateCodeVerifier();
		String codeChallenge = oauthClient.generateCodeChallenge(codeVerifier);
		// when
		String actual = oauthClient.createAuthURL(state, codeVerifier);

		// then
		String expected = "https://kauth.kakao.com/oauth/authorize?"
			+ "response_type=code"
			+ "&client_id=88171956c99257e9ea8c241b4fd544df"
			+ "&redirect_uri=https://localhost/api/auth/kakao/login"
			+ "&scope=openid"
			+ "&state=" + state
			+ "&code_challenge=" + codeChallenge
			+ "&code_challenge_method=S256";
		log.info("actual={}", actual);
		Assertions.assertThat(actual).isEqualTo(expected);
	}
}
