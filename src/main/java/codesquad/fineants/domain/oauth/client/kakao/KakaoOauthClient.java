package codesquad.fineants.domain.oauth.client.kakao;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.domain.oauth.client.DecodedIdTokenPayload;
import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.properties.OauthProperties;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
import codesquad.fineants.spring.util.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KakaoOauthClient extends OauthClient {
	private final String scope;
	private final String iss;
	private final String aud;

	private final OauthProperties.Kakao.AuthorizationCode properties;

	public KakaoOauthClient(OauthProperties.Kakao kakao) {
		super(kakao.getClientId(),
			kakao.getClientSecret(),
			kakao.getTokenUri(),
			null,
			kakao.getRedirectUri(),
			kakao.getPublicKeyUri(),
			kakao.getAuthorizeUri(),
			kakao.getResponseType());
		this.scope = kakao.getScope();
		this.iss = kakao.getIss();
		this.aud = kakao.getAud();
		this.properties = kakao.getAuthorizationCode();
	}

	@Override
	public MultiValueMap<String, String> createTokenBody(final String authorizationCode, final String redirectUri,
		final String codeVerifier, String state) {
		final String grantType = "authorization_code";
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add(properties.getCode(), authorizationCode);
		formData.add(properties.getClientId(), getClientId());
		formData.add(properties.getClientSecret(), getClientSecret());
		formData.add(properties.getRedirectUri(), redirectUri);
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
	protected DecodedIdTokenPayload deserializeDecodedPayload(String decodedPayload) {
		return ObjectMapperUtil.deserialize(decodedPayload, KakaoDecodedIdTokenPayload.class);
	}

	@Override
	public void validatePayload(DecodedIdTokenPayload payload, LocalDateTime now, String nonce) {
		payload.validateIdToken(iss, aud, now, nonce);
	}

	@Override
	public boolean isSupportOICD() {
		return true;
	}
}
