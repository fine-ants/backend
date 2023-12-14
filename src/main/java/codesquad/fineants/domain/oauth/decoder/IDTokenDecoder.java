package codesquad.fineants.domain.oauth.decoder;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

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

@Slf4j
public abstract class IDTokenDecoder {

	private static final String ID_TOKEN_SEPARATOR = "\\.";
	private static final int PAYLOAD = 1;

	protected abstract DecodedIdTokenPayload deserializeDecodedPayload(String payload);

	public DecodedIdTokenPayload decode(String idToken, String jwkUri) {
		verifyIdToken(idToken, jwkUri);
		return deserializeDecodedPayload(decodePayload(parsePayloadBlock(idToken)));
	}

	private String parsePayloadBlock(String idToken) {
		return idToken.split(ID_TOKEN_SEPARATOR)[PAYLOAD];
	}

	private String decodePayload(String payload) {
		return new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
	}

	private void verifyIdToken(String idToken, String jwkUri) {
		Jwk jwk = getJwkBy(fetchJwkProvider(jwkUri), parseKid(idToken));
		Algorithm algorithm = getAlgorithmBy(jwk);
		JWTVerifier verifier = JWT.require(algorithm).build();
		verifier.verify(idToken);
	}

	private JwkProvider fetchJwkProvider(String jwkUri) {
		try {
			return new UrlJwkProvider(new URL(jwkUri));
		} catch (MalformedURLException e) {
			throw new BadRequestException(JwkErrorCode.INVALID_JWK_URI);
		}
	}

	private String parseKid(String idToken) {
		try {
			return JWT.decode(idToken).getKeyId();
		} catch (JWTDecodeException e) {
			throw new BadRequestException(JwkErrorCode.INVALID_ID_TOKEN);
		}
	}

	private Jwk getJwkBy(JwkProvider provider, String kid) {
		try {
			return provider.get(kid);
		} catch (JwkException e) {
			throw new NotFoundResourceException(JwkErrorCode.NOT_FOUND_SIGNING_KEY);
		}
	}

	private Algorithm getAlgorithmBy(Jwk jwk) {
		Algorithm algorithm;
		try {
			algorithm = Algorithm.RSA256((RSAPublicKey)jwk.getPublicKey(), null);
		} catch (InvalidPublicKeyException e) {
			throw new BadRequestException(JwkErrorCode.INVALID_PUBLIC_KEY);
		}
		return algorithm;
	}
}
