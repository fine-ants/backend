package codesquad.fineants.domain.oauth.client;

import static org.springframework.http.HttpHeaders.*;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
import codesquad.fineants.spring.api.member.service.JwkProviderSingleton;
import codesquad.fineants.spring.util.ObjectMapperUtil;
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
	private final ObjectMapperUtil objectMapperUtil;

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

	public DecodedIdTokenPayload decodeIdToken(String idToken, String nonce, List<OauthPublicKey> publicKeys) {
		final String separatorRegex = "\\.";
		final int payloadIndex = 1;
		String payload = idToken.split(separatorRegex)[payloadIndex];
		byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
		String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);
		DecodedIdTokenPayload decodedIdTokenPayload = objectMapperUtil.deserialize(decodedPayload,
			DecodedIdTokenPayload.class);

		validatePayload(decodedIdTokenPayload, nonce);
		validateSign(idToken, publicKeys);
		return decodedIdTokenPayload;
	}

	public abstract void validatePayload(DecodedIdTokenPayload payload, String nonce);

	public void validateSign(String idToken, List<OauthPublicKey> publicKeys) {
		String[] split = idToken.split("\\.");
		String payload = split[0];
		byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
		String decodedHeader = new String(decodedBytes, StandardCharsets.UTF_8);
		DecodedIdTokenHeader header = objectMapperUtil.deserialize(decodedHeader, DecodedIdTokenHeader.class);

		// 공개키 목록에서 헤더의 kid에 해당하는 공개키 값 확인
		publicKeys.stream()
			.filter(oauthPublicKey -> oauthPublicKey.equalKid(header.getKid()))
			.findAny()
			.orElseThrow(() -> new BadRequestException(OauthErrorCode.WRONG_ID_TOKEN,
				"kid 값이 일치하지 않습니다. header.kid=" + header.getKid()));

		// 검증 없이 디코딩
		DecodedJWT jwtOrigin = JWT.decode(idToken);

		// 공개키 프로바이더 준비
		JwkProvider provider = JwkProviderSingleton.getInstance();
		Jwk jwk;
		try {
			jwk = provider.get(jwtOrigin.getKeyId());
		} catch (JwkException e) {
			throw new RuntimeException(e);
		}

		// 검증 및 디코딩
		Algorithm algorithm;
		try {
			algorithm = Algorithm.RSA256((RSAPublicKey)jwk.getPublicKey(), null);
		} catch (InvalidPublicKeyException e) {
			throw new RuntimeException(e);
		}
		JWTVerifier verifier = JWT.require(algorithm).build();
		verifier.verify(idToken);
	}
}
