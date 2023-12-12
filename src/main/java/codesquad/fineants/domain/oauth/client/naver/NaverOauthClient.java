package codesquad.fineants.domain.oauth.client.naver;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.domain.oauth.client.DecodedIdTokenPayload;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.properties.OauthProperties;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.response.OauthUserProfile;
import codesquad.fineants.spring.api.member.service.WebClientWrapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NaverOauthClient extends OauthClient {

	public NaverOauthClient(OauthProperties.Naver naver, WebClientWrapper webClient) {
		super(naver.getClientId(),
			naver.getClientSecret(),
			naver.getTokenUri(),
			naver.getUserInfoUri(),
			naver.getRedirectUri(),
			null,
			naver.getAuthorizeUri(),
			naver.getResponseType(),
			webClient);
	}

	@Override
	public MultiValueMap<String, String> createTokenBody(Map<String, String> bodyMap) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("code", bodyMap.get("code"));
		formData.add("client_id", getClientId());
		formData.add("client_secret", getClientSecret());
		formData.add("redirect_uri", bodyMap.get("redirectUrl"));
		formData.add("state", bodyMap.get("state"));
		formData.add("grant_type", "authorization_code");
		return formData;
	}

	@Override
	public OauthUserProfile createOauthUserProfileResponse(Map<String, Object> attributes) {
		Map<String, Object> responseMap = (Map<String, Object>)attributes.get("response");
		String email = (String)responseMap.get("email");
		String profileImage = (String)responseMap.get("profile_image");
		return new OauthUserProfile(email, profileImage, "naver");
	}

	@Override
	public String createAuthURL(AuthorizationRequest request) {
		return getAuthorizeUri() + "?"
			+ "response_type=" + getResponseType() + "&"
			+ "client_id=" + getClientId() + "&"
			+ "redirect_uri=" + getRedirectUri() + "&"
			+ "state=" + request.getState();
	}

	@Override
	public DecodedIdTokenPayload deserializeDecodedPayload(String decodedPayload) {
		throw new IllegalStateException("네이버는 지원하지 않는 기능입니다.");
	}

	@Override
	public void validatePayload(DecodedIdTokenPayload payload, LocalDateTime now, String nonce) {
		throw new IllegalStateException("네이버는 지원하지 않는 기능입니다.");
	}

	@Override
	public boolean isSupportOICD() {
		return false;
	}
}
