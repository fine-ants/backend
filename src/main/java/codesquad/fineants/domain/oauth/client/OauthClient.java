package codesquad.fineants.domain.oauth.client;

import static org.springframework.http.HttpHeaders.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;

import codesquad.fineants.spring.api.errors.errorcode.JwkErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.response.OauthUserProfile;
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
	private final String authorizeUri;
	private final String responseType;

	public abstract MultiValueMap<String, String> createTokenBody(String authorizationCode, String redirectUri,
		String codeVerifier, String state);

	public abstract OauthUserProfile createOauthUserProfileResponse(Map<String, Object> attributes);

	public abstract String createAuthURL(AuthorizationRequest authorizationRequest);

	public abstract boolean isSupportOICD();

	public MultiValueMap<String, String> createTokenHeader() {
		MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
		result.add(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString());
		result.add(ACCEPT, MediaType.APPLICATION_JSON.toString());
		result.add(ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
		return result;
	}

	public DecodedIdTokenPayload decodeIdToken(String idToken, String nonce, LocalDateTime now) {
		final String separatorRegex = "\\.";
		final int payloadIndex = 1;
		String payload = idToken.split(separatorRegex)[payloadIndex];
		byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
		String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);
		DecodedIdTokenPayload decodedIdTokenPayload = deserializeDecodedPayload(decodedPayload);

		validateSign(idToken);
		validatePayload(decodedIdTokenPayload, now, nonce);
		return decodedIdTokenPayload;
	}

	protected abstract DecodedIdTokenPayload deserializeDecodedPayload(String decodedPayload);

	public abstract void validatePayload(DecodedIdTokenPayload payload, LocalDateTime now, String nonce);

	public void validateSign(String idToken) {
		// 검증 없이 디코딩
		String kid;
		try {
			kid = JWT.decode(idToken).getKeyId();
		} catch (JWTDecodeException e) {
			log.error(e.getMessage(), e);
			throw new BadRequestException(JwkErrorCode.INVALID_ID_TOKEN);
		}

		// 공개키 프로바이더 준비
		JwkProvider provider;
		try {
			provider = new UrlJwkProvider(new URL(getPublicKeyUri()));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		Jwk jwk;
		try {
			jwk = provider.get(kid);
		} catch (JwkException e) {
			log.error(e.getMessage());
			throw new NotFoundResourceException(JwkErrorCode.NOT_FOUND_SIGNING_KEY);
		}

		// 검증 및 디코딩
		Algorithm algorithm;
		try {
			algorithm = Algorithm.RSA256((RSAPublicKey)jwk.getPublicKey(), null);
		} catch (InvalidPublicKeyException e) {
			throw new BadRequestException(JwkErrorCode.INVALID_PUBLIC_KEY);
		}
		JWTVerifier verifier = JWT.require(algorithm).build();
		verifier.verify(idToken);
	}
}
