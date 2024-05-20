package codesquad.fineants.domain.oauth.client.google;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.domain.member.domain.dto.request.AuthorizationRequest;
import codesquad.fineants.domain.member.domain.dto.response.OauthToken;
import codesquad.fineants.domain.member.domain.dto.response.OauthUserProfile;
import codesquad.fineants.domain.member.service.WebClientWrapper;
import codesquad.fineants.domain.oauth.client.DecodedIdTokenPayload;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.decoder.IDTokenDecoder;
import codesquad.fineants.domain.oauth.properties.OauthProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleOauthClient extends OauthClient {

	private final String scope;
	private final String iss;
	private final String aud;

	public GoogleOauthClient(OauthProperties.Google google, WebClientWrapper webClient, IDTokenDecoder decoder) {
		super(google.getClientId(),
			google.getClientSecret(),
			google.getTokenUri(),
			null,
			google.getRedirectUri(),
			google.getJwksUri(),
			google.getAuthorizeUri(),
			google.getResponseType(),
			webClient,
			decoder);
		this.scope = google.getScope();
		this.iss = google.getIss();
		this.aud = google.getAud();
	}

	@Override
	protected MultiValueMap<String, String> createTokenBody(Map<String, String> bodyMap) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("code", bodyMap.get("code"));
		formData.add("client_id", getClientId());
		formData.add("client_secret", getClientSecret());
		formData.add("redirect_uri", bodyMap.get("redirectUrl"));
		formData.add("code_verifier", bodyMap.get("codeVerifier"));
		formData.add("grant_type", "authorization_code");
		return formData;
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
	protected boolean isSupportOIDC() {
		return true;
	}

	@Override
	protected void validatePayload(DecodedIdTokenPayload payload, LocalDateTime now, String nonce) {
		payload.validateIdToken(iss, aud, now, nonce);
	}

	@Override
	protected OauthUserProfile fetchUserProfile(OauthToken oauthToken) {
		throw new IllegalStateException("GoogleOauthClient 객체는 해당 기능을 지원하지 않습니다.");
	}

	@Override
	protected OauthUserProfile fetchUserProfile(DecodedIdTokenPayload payload) {
		return OauthUserProfile.google(payload);
	}
}
