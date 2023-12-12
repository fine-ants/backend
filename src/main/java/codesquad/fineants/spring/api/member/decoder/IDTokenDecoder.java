package codesquad.fineants.spring.api.member.decoder;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;

import codesquad.fineants.domain.oauth.client.DecodedIdTokenPayload;
import codesquad.fineants.spring.api.errors.errorcode.JwkErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public abstract class IDTokenDecoder {

	private static final String ID_TOKEN_SEPARATOR = "\\.";
	private static final int PAYLOAD = 1;

	protected abstract DecodedIdTokenPayload deserializeDecodedPayload(String payload);

	public DecodedIdTokenPayload decode(String idToken, String jwkUri) {
		String decodedPayload = decodePayload(parsePayloadBlock(idToken));
		DecodedIdTokenPayload decodedIdTokenPayload = deserializeDecodedPayload(decodedPayload);
		validateSign(idToken, jwkUri);
		return decodedIdTokenPayload;
	}

	private static String parsePayloadBlock(String idToken) {
		return idToken.split(ID_TOKEN_SEPARATOR)[PAYLOAD];
	}

	private static String decodePayload(String payload) {
		return new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
	}

	private void validateSign(String idToken, String jwkUri) {
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
			provider = new UrlJwkProvider(new URL(jwkUri));
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
