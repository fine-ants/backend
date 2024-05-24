package codesquad.fineants.global.security.oauth.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@Getter
public class Token {
	private final String accessToken;
	private final String refreshToken;

	public static Token create(String accessToken, String refreshToken) {
		return new Token(accessToken, refreshToken);
	}
}
