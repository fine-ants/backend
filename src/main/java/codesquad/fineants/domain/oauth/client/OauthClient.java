package codesquad.fineants.domain.oauth.client;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import codesquad.fineants.spring.api.member.response.OauthAccessTokenResponse;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public abstract class OauthClient {

	private final String clientId;
	private final String clientSecret;
	private final String tokenUri;
	private final String userInfoUri;
	private final String redirectUri;

	public OauthClient(String clientId, String clientSecret, String tokenUri, String userInfoUri, String redirectUri) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.tokenUri = tokenUri;
		this.userInfoUri = userInfoUri;
		this.redirectUri = redirectUri;
	}

	public abstract OauthAccessTokenResponse exchangeAccessTokenByAuthorizationCode(String authorizationCode,
		String redirectUrl);

	public abstract MultiValueMap<String, String> createFormData(String authorizationCode, String redirectUrl);

	public Map<String, Object> getUserAttributes(String userInfoUri, OauthAccessTokenResponse accessTokenResponse) {
		return WebClient.create()
			.get()
			.uri(userInfoUri)
			.headers(header -> header.setBearerAuth(accessTokenResponse.getAccessToken()))
			.retrieve()
			.bodyToMono(
				new ParameterizedTypeReference<Map<String, Object>>() { // response body를 Map<String,Object> 형태로 변환
				})
			.block();
	}

	public abstract OauthUserProfileResponse createOauthUserProfileResponse(Map<String, Object> attributes);

	public OauthUserProfileResponse getUserProfileByAccessToken(OauthAccessTokenResponse accessTokenResponse) {
		Map<String, Object> userProfileMap = getUserAttributes(userInfoUri, accessTokenResponse);
		log.debug("userProfileMap : {}", userProfileMap);
		return createOauthUserProfileResponse(userProfileMap);
	}
}
