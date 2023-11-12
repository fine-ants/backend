package codesquad.fineants.domain.oauth.client;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.domain.oauth.properties.OauthProperties;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NaverOauthClient extends OauthClient {

	private final String authorizeUri;
	private final String responseType;

	public NaverOauthClient(OauthProperties.Naver naver) {
		super(naver.getClientId(),
			naver.getClientSecret(),
			naver.getTokenUri(),
			naver.getUserInfoUri(),
			naver.getRedirectUri(),
			null);
		this.authorizeUri = naver.getAuthorizeUri();
		this.responseType = naver.getResponseType();
	}

	@Override
	public MultiValueMap<String, String> createTokenBody(String authorizationCode, String codeVerifier) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("grant_type", "authorization_code");
		formData.add("code", authorizationCode);
		formData.add("redirect_uri", getRedirectUri());
		return formData;
	}

	@Override
	public OauthUserProfileResponse createOauthUserProfileResponse(Map<String, Object> attributes) {
		Map<String, Object> responseMap = (Map<String, Object>)attributes.get("response");
		String email = (String)responseMap.get("email");
		String profileImage = (String)responseMap.get("profile_image");
		return new OauthUserProfileResponse(email, profileImage);
	}

	@Override
	public String createAuthURL(AuthorizationRequest request) {
		return authorizeUri + "?"
			+ "response_type=" + responseType + "&"
			+ "client_id=" + getClientId() + "&"
			+ "redirect_uri=" + getRedirectUri() + "&"
			+ "state=" + request.getState();
	}

	@Override
	public void validatePayload(DecodedIdTokenPayload payload, LocalDateTime now, String nonce) {

	}
}
