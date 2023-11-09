package codesquad.fineants.spring.api.member.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthorizationRequest {
	private String codeVerifier;
	private String nonce;

	public static AuthorizationRequest of(String codeVerifier, String nonce) {
		return new AuthorizationRequest(codeVerifier, nonce);
	}
}
