package codesquad.fineants.domain.oauth.client;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.oauth.properties.OauthProperties;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
import codesquad.fineants.spring.api.member.service.JwkProviderSingleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KakaoOauthClient extends OauthClient {

	private final String authorizeUri;
	private final String responseType;
	private final String scope;
	private final String iss;
	private final String aud;

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
	}

	@Override
	public MultiValueMap<String, String> createFormData(String authorizationCode, String codeVerifier) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("code", authorizationCode);
		formData.add("client_id", getClientId());
		formData.add("client_secret", getClientSecret());
		formData.add("redirect_uri", getRedirectUri());
		formData.add("code_verifier", codeVerifier);
		formData.add("grant_type", "authorization_code");
		return formData;
	}

	@Override
	public OauthUserProfileResponse createOauthUserProfileResponse(Map<String, Object> attributes) {
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
	public DecodedIdTokenPayload validateIdToken(String idToken, String nonce, List<OauthPublicKey> publicKeys) {
		DecodedIdTokenPayload payload = validatePayload(idToken, nonce);
		validateSign(idToken, publicKeys);
		return payload;
	}

	private DecodedIdTokenPayload validatePayload(String idToken, String nonce) {
		String[] split = idToken.split("\\.");
		String payload = split[1];
		byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
		String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);
		DecodedIdTokenPayload decodedIdTokenPayload = deserializeFromJson(decodedPayload,
			DecodedIdTokenPayload.class);
		decodedIdTokenPayload.validateIdToken(iss, aud, LocalDateTime.now(), nonce);
		log.info("{}", decodedIdTokenPayload);
		return decodedIdTokenPayload;
	}

	private void validateSign(String idToken, List<OauthPublicKey> publicKeys) {
		String[] split = idToken.split("\\.");
		String payload = split[0];
		byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
		String decodedHeader = new String(decodedBytes, StandardCharsets.UTF_8);
		DecodedIdTokenHeader header = deserializeFromJson(decodedHeader, DecodedIdTokenHeader.class);

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
		DecodedJWT jwt = verifier.verify(idToken);
		log.info("jwt : {}", jwt);
	}

	private <T> T deserializeFromJson(String json, Class<T> returnType) {
		try {
			return new ObjectMapper().readValue(json, returnType);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException("역직렬화에 실패하였습니다.");
		}
	}
}
