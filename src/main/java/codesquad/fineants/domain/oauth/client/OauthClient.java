package codesquad.fineants.domain.oauth.client;

import static org.springframework.http.HttpHeaders.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.domain.member.domain.dto.request.AuthorizationRequest;
import codesquad.fineants.domain.member.domain.dto.request.OauthMemberLoginRequest;
import codesquad.fineants.domain.member.domain.dto.response.OauthToken;
import codesquad.fineants.domain.member.domain.dto.response.OauthUserProfile;
import codesquad.fineants.domain.member.service.WebClientWrapper;
import codesquad.fineants.domain.oauth.decoder.IDTokenDecoder;
import codesquad.fineants.global.errors.errorcode.OauthErrorCode;
import codesquad.fineants.global.errors.exception.ServerInternalException;
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
	private final String jwkUri;
	private final String authorizeUri;
	private final String responseType;
	private final WebClientWrapper webClient;
	private final IDTokenDecoder idTokenDecoder;

	public abstract String createAuthURL(AuthorizationRequest authorizationRequest);

	protected abstract MultiValueMap<String, String> createTokenBody(Map<String, String> bodyMap);

	protected abstract boolean isSupportOIDC();

	protected abstract void validatePayload(DecodedIdTokenPayload payload, LocalDateTime now, String nonce);

	protected abstract OauthUserProfile fetchUserProfile(OauthToken oauthToken);

	protected abstract OauthUserProfile fetchUserProfile(DecodedIdTokenPayload payload);

	public OauthUserProfile fetchProfile(OauthMemberLoginRequest request, AuthorizationRequest authRequest) {
		// OIDC 토큰 및 Access Token 발급
		Map<String, String> tokenBodyMap = request.toTokenBodyMap();
		tokenBodyMap.putAll(authRequest.toTokenBodyMap());
		OauthToken oauthToken = retrieveToken(tokenBodyMap);

		// 프로필 정보 가져오기
		if (isSupportOIDC()) {
			DecodedIdTokenPayload payload = idTokenDecoder.decode(oauthToken.getIdToken(), jwkUri);
			validatePayload(payload, request.getRequestTime(), authRequest.getNonce());
			return fetchUserProfile(payload);
		}
		return fetchUserProfile(oauthToken);
	}

	private OauthToken retrieveToken(Map<String, String> tokenBodyMap) {
		OauthToken oauthToken = webClient.post(tokenUri,
			createTokenHeader(),
			createTokenBody(tokenBodyMap),
			OauthToken.class);
		if (oauthToken.isEmpty()) {
			throw new ServerInternalException(OauthErrorCode.FAIL_ACCESS_TOKEN);
		}
		return oauthToken;
	}

	private MultiValueMap<String, String> createTokenHeader() {
		MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
		result.add(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString());
		result.add(ACCEPT, MediaType.APPLICATION_JSON.toString());
		result.add(ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
		return result;
	}
}
