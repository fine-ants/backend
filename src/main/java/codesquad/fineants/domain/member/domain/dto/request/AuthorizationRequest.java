package codesquad.fineants.domain.member.domain.dto.request;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthorizationRequest {
	private String state;
	private String codeVerifier;
	private String codeChallenge;
	private String nonce;
	private long createTime;

	public static AuthorizationRequest of(String state, String codeVerifier, String codeChallenge, String nonce) {
		return of(state, codeVerifier, codeChallenge, nonce, System.currentTimeMillis());
	}

	public static AuthorizationRequest of(String state, String codeVerifier, String codeChallenge, String nonce,
		long createTime) {
		return new AuthorizationRequest(state, codeVerifier, codeChallenge, nonce, createTime);
	}

	public boolean isExpiration(long time) {
		return time - createTime > 60000;
	}

	public Map<String, String> toTokenBodyMap() {
		Map<String, String> result = new HashMap<>();
		result.put("codeVerifier", codeVerifier);
		return result;
	}
}
