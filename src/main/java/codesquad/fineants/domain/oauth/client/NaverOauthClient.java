package codesquad.fineants.domain.oauth.client;

import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.domain.oauth.properties.OauthProperties;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NaverOauthClient extends OauthClient {

	public NaverOauthClient(OauthProperties.Naver naver) {
		super(naver.getClientId(),
			naver.getClientSecret(),
			naver.getTokenUri(),
			naver.getUserInfoUri(),
			naver.getRedirectUri());
	}

	@Override
	public MultiValueMap<String, String> createFormData(String authorizationCode, String codeVerifier) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("code", authorizationCode);
		formData.add("redirect_uri", getRedirectUri());
		formData.add("grant_type", "authorization_code");
		formData.add("code_verifier", codeVerifier);
		return formData;
	}

	@Override
	public OauthUserProfileResponse createOauthUserProfileResponse(Map<String, Object> attributes) {
		Map<String, Object> responseMap = (Map<String, Object>)attributes.get("response");
		String email = (String)responseMap.get("email");
		String profileImage = (String)responseMap.get("profile_image");
		return new OauthUserProfileResponse(email, profileImage);
	}

	// TODO: 구현예정
	@Override
	public String createAuthURL(String state, String codeVerifier) {
		return null;
	}
}
