package codesquad.fineants.domain.oauth.client;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.domain.oauth.repository.OauthClientRepository;
import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.member.domain.dto.request.AuthorizationRequest;

class KakaoOauthClientTest extends AbstractContainerBaseTest {

	@Autowired
	private OauthClientRepository oauthClientRepository;

	@Autowired
	private AuthorizationCodeRandomGenerator authorizationCodeRandomGenerator;

	@DisplayName("인가코드 URL을 생성한다")
	@Test
	void createAuthURL() {
		// given
		OauthClient oauthClient = oauthClientRepository.findOneBy("kakao");
		String state = authorizationCodeRandomGenerator.generateState();
		String codeVerifier = authorizationCodeRandomGenerator.generateCodeVerifier();
		String nonce = authorizationCodeRandomGenerator.generateNonce();
		String codeChallenge = authorizationCodeRandomGenerator.generateCodeChallenge(codeVerifier);
		AuthorizationRequest authorizationRequest = AuthorizationRequest.of(state, codeVerifier, codeChallenge, nonce);
		// when
		String actual = oauthClient.createAuthURL(authorizationRequest);

		// then
		String expected = "https://kauth.kakao.com/oauth/authorize?"
			+ "response_type=code"
			+ "&client_id=dfb1e25a2b97d03b0b225d4874a34823"
			+ "&redirect_uri=http://localhost:5173/signin/loading?provider=kakao"
			+ "&scope=openid"
			+ "&state=" + state
			+ "&nonce=" + nonce
			+ "&code_challenge=" + codeChallenge
			+ "&code_challenge_method=S256";
		assertThat(actual).isEqualTo(expected);
	}
}
