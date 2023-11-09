package codesquad.fineants.domain.oauth.client;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OauthClientRandomGenerator {

	public String generateState() {
		SecureRandom secureRandom = new SecureRandom();
		return new BigInteger(130, secureRandom).toString();
	}

	public String generateCodeVerifier() {
		SecureRandom secureRandom = new SecureRandom();
		byte[] codeVerifier = new byte[32];
		secureRandom.nextBytes(codeVerifier);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
	}

	public String generateNonce() {
		SecureRandom secureRandom = new SecureRandom();
		byte[] randomBytes = new byte[16];
		secureRandom.nextBytes(randomBytes);
		BigInteger nonceValue = new BigInteger(1, randomBytes);
		return nonceValue.toString(16); // 16진수 문자열로 반환
	}

	public String generateCodeChallenge(String codeVerifier) {
		byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		messageDigest.update(bytes, 0, bytes.length);
		byte[] digest = messageDigest.digest();
		return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
	}
}
