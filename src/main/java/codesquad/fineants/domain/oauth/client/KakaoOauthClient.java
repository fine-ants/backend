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
public class KakaoOauthClient extends OauthClient {

	private final String authorizeUri;
	private final String responseType;
	private final String scope;
	private final String iss;
	private final String aud;

	private final OauthProperties.Kakao.AuthorizationCode properties;

	public KakaoOauthClient(OauthProperties.Kakao kakao) {
		super(kakao.getClientId(),
			kakao.getClientSecret(),
			kakao.getTokenUri(),
			kakao.getUserInfoUri(),
			kakao.getRedirectUri(),
			kakao.getPublicKeyUri());
		this.authorizeUri = kakao.getAuthorizeUri();
		this.responseType = kakao.getResponseType();
		this.scope = kakao.getScope();
		this.iss = kakao.getIss();
		this.aud = kakao.getAud();
		this.properties = kakao.getAuthorizationCode();
	}

	@Override
	public MultiValueMap<String, String> createTokenBody(final String authorizationCode, final String codeVerifier) {
		final String grantType = "authorization_code";
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add(properties.getCode(), authorizationCode);
		formData.add(properties.getClientId(), getClientId());
		formData.add(properties.getClientSecret(), getClientSecret());
		formData.add(properties.getRedirectUri(), getRedirectUri());
		formData.add(properties.getCodeVerifier(), codeVerifier);
		formData.add(properties.getGrantType(), grantType);
		return formData;
	}

	@Override
	public OauthUserProfileResponse createOauthUserProfileResponse(final Map<String, Object> attributes) {
		String email = (String)attributes.get("email");
		String picture = (String)attributes.get("picture");
		return new OauthUserProfileResponse(email, picture);
	}

	@Override
	public String createAuthURL(AuthorizationRequest request) {
		return authorizeUri + "?"
			+ "response_type=" + responseType + "&"
			+ "client_id=" + getClientId() + "&"
			+ "redirect_uri=" + getRedirectUri() + "&"
			+ "scope=" + scope + "&"
			+ "state=" + request.getState() + "&"
			+ "nonce=" + request.getNonce() + "&"
			+ "code_challenge=" + request.getCodeChallenge() + "&"
			+ "code_challenge_method=S256";
	}

	@Override
	public void validatePayload(DecodedIdTokenPayload payload, String nonce) {
		payload.validateIdToken(iss, aud, LocalDateTime.now(), nonce);
	}
}
