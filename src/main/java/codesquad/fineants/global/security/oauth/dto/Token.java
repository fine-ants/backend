package codesquad.fineants.global.security.oauth.dto;

import jakarta.servlet.http.Cookie;
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

	public Cookie createAccessTokenCookie() {
		Cookie result = new Cookie("accessToken", accessToken);
		result.setSecure(true);
		result.setHttpOnly(true);
		return result;
	}

	public Cookie createRefreshTokenCookie() {
		Cookie result = new Cookie("refreshToken", refreshToken);
		result.setSecure(true);
		result.setHttpOnly(true);
		return result;
	}
}
