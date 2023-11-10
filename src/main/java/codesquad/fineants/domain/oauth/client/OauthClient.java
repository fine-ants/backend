package codesquad.fineants.domain.oauth.client;

import static org.springframework.http.HttpHeaders.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@RequiredArgsConstructor
@Slf4j
public abstract class OauthClient {

	private final String clientId;
	private final String clientSecret;
	private final String tokenUri;
	private final String userInfoUri;
	private final String redirectUri;
	private final String publicKeyUri;

	public abstract MultiValueMap<String, String> createTokenBody(String authorizationCode, String codeVerifier);

	public abstract OauthUserProfileResponse createOauthUserProfileResponse(Map<String, Object> attributes);

	public abstract String createAuthURL(AuthorizationRequest authorizationRequest);

	public MultiValueMap<String, String> createTokenHeader() {
		MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
		result.add(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString());
		result.add(ACCEPT, MediaType.APPLICATION_JSON.toString());
		result.add(ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
		return result;
	}
	
	public abstract DecodedIdTokenPayload validateIdToken(String idToken, String nonce,
		List<OauthPublicKey> publicKeys);
}
