package codesquad.fineants.domain.oauth.client.google;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.domain.oauth.client.DecodedIdTokenPayload;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.properties.OauthProperties;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.response.OauthUserProfile;
import codesquad.fineants.spring.util.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleOauthClient extends OauthClient {

	private final String scope;
	private final String iss;
	private final String aud;

	public GoogleOauthClient(OauthProperties.Google google) {
		super(google.getClientId(),
			google.getClientSecret(),
			google.getTokenUri(),
			null,
			google.getRedirectUri(),
			google.getJwksUri(),
			google.getAuthorizeUri(),
			google.getResponseType());
		this.scope = google.getScope();
		this.iss = google.getIss();
		this.aud = google.getAud();
	}

	@Override
	public MultiValueMap<String, String> createTokenBody(String authorizationCode, String redirectUri,
		String codeVerifier, String state) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("code", authorizationCode);
		formData.add("client_id", getClientId());
		formData.add("client_secret", getClientSecret());
		formData.add("redirect_uri", redirectUri);
		formData.add("code_verifier", codeVerifier);
		formData.add("grant_type", "authorization_code");
		return formData;
	}

	@Override
	public OauthUserProfile createOauthUserProfileResponse(Map<String, Object> attributes) {
		String email = (String)attributes.get("email");
		String picture = (String)attributes.get("picture");
		return new OauthUserProfile(email, picture);
	}

	@Override
	public String createAuthURL(AuthorizationRequest request) {
		return getAuthorizeUri() + "?"
			+ "response_type=" + getResponseType() + "&"
			+ "client_id=" + getClientId() + "&"
			+ "redirect_uri=" + getRedirectUri() + "&"
			+ "scope=" + scope + "&"
			+ "state=" + request.getState() + "&"
			+ "nonce=" + request.getNonce() + "&"
			+ "code_challenge=" + request.getCodeChallenge() + "&"
			+ "code_challenge_method=S256";
	}

	@Override
	public boolean isSupportOICD() {
		return true;
	}

	@Override
	protected DecodedIdTokenPayload deserializeDecodedPayload(String decodedPayload) {
		return ObjectMapperUtil.deserialize(decodedPayload, GoogleDecodedIdTokenPayload.class);
	}

	@Override
	public void validatePayload(DecodedIdTokenPayload payload, LocalDateTime now, String nonce) {
		payload.validateIdToken(iss, aud, now, nonce);
	}
}
