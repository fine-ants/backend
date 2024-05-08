package codesquad.fineants.domain.oauth.client;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.member.domain.dto.request.AuthorizationRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthorizationCodeRandomGenerator {

	public AuthorizationRequest generateAuthorizationRequest() {
		final String state = generateState();
		final String codeVerifier = generateCodeVerifier();
		final String codeChallenge = generateCodeChallenge(codeVerifier);
		final String nonce = generateNonce();
		return AuthorizationRequest.of(
			state,
			codeVerifier,
			codeChallenge,
			nonce
		);
	}

	public String generateState() {
		final SecureRandom secureRandom = new SecureRandom();
		return new BigInteger(130, secureRandom).toString();
	}

	public String generateCodeVerifier() {
		final SecureRandom secureRandom = new SecureRandom();
		final byte[] codeVerifier = new byte[32];
		secureRandom.nextBytes(codeVerifier);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
	}

	public String generateNonce() {
		final SecureRandom secureRandom = new SecureRandom();
		final byte[] randomBytes = new byte[16];
		secureRandom.nextBytes(randomBytes);
		final BigInteger nonceValue = new BigInteger(1, randomBytes);
		return nonceValue.toString(16); // 16진수 문자열로 반환
	}

	public String generateCodeChallenge(final String codeVerifier) {
		final byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
		final String algorithm = "SHA-256";
		final MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		messageDigest.update(bytes, 0, bytes.length);
		final byte[] digest = messageDigest.digest();
		return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
	}
}
