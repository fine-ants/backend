package codesquad.fineants.domain.oauth.client;

import static org.springframework.http.HttpHeaders.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
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

	public abstract MultiValueMap<String, String> createFormData(String authorizationCode, String codeVerifier);

	public abstract OauthUserProfileResponse createOauthUserProfileResponse(Map<String, Object> attributes);

	public abstract String createAuthURL(String state, String codeVerifier);

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

	public MultiValueMap<String, String> createTokenHeader() {
		MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
		result.add(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString());
		result.add(ACCEPT, MediaType.APPLICATION_JSON.toString());
		result.add(ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
		return result;
	}

	public MultiValueMap<String, String> createUserInfoHeader(String tokenType, String accessToken) {
		MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
		result.add(AUTHORIZATION, String.format("%s %s", tokenType, accessToken));
		return result;
	}
}
