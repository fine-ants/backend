package codesquad.fineants.domain.oauth.client;

import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.domain.oauth.properties.OauthProperties;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KakaoOauthClient extends OauthClient {

	private final String authorizeUri;
	private final String responseType;
	private final String scope;
	private final String oicdUserInfoUri;

	public KakaoOauthClient(OauthProperties.Kakao kakao) {
		super(kakao.getClientId(),
			kakao.getClientSecret(),
			kakao.getTokenUri(),
			kakao.getUserInfoUri(),
			kakao.getRedirectUri());
		this.authorizeUri = kakao.getAuthorizeUri();
		this.responseType = kakao.getResponseType();
		this.scope = kakao.getScope();
		this.oicdUserInfoUri = kakao.getOicdUserInfoUri();
	}

	@Override
	public MultiValueMap<String, String> createFormData(String authorizationCode, String codeVerifier) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("code", authorizationCode);
		formData.add("client_id", getClientId());
		formData.add("client_secret", getClientSecret());
		formData.add("redirect_uri", getRedirectUri());
		formData.add("code_verifier", codeVerifier);
		formData.add("grant_type", "authorization_code");
		return formData;
	}

	@Override
	public OauthUserProfileResponse createOauthUserProfileResponse(Map<String, Object> attributes) {
		Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
		Map<String, Object> profile = (Map<String, Object>)kakaoAccount.get("profile");
		log.info("profile={}", profile);
		String email = (String)kakaoAccount.get("email");
		String profileImage = (String)profile.get("profile_image_url");
		return new OauthUserProfileResponse(email, profileImage);
	}

	private OauthUserProfileResponse createOicdUserProfileResponse(Map<String, Object> attributes) {
		String email = (String)attributes.get("email");
		String picture = (String)attributes.get("picture");
		return new OauthUserProfileResponse(email, picture);
	}

	@Override
	public String createAuthURL(String state, String codeVerifier) {
		return authorizeUri + "?"
			+ "response_type=" + responseType + "&"
			+ "client_id=" + getClientId() + "&"
			+ "redirect_uri=" + getRedirectUri() + "&"
			+ "scope=" + scope + "&"
			+ "state=" + state + "&"
			+ "code_challenge=" + generateCodeChallenge(codeVerifier) + "&"
			+ "code_challenge_method=S256";
	}
}
