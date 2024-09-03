package co.fineants.api.global.security.factory;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import co.fineants.api.global.security.oauth.dto.Token;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TokenFactory {

	public ResponseCookie createAccessTokenCookie(Token token) {
		return token.createAccessTokenCookie()
			.sameSite("None")
			.path("/")
			.secure(true)
			.httpOnly(true)
			.build();
	}

	public ResponseCookie createRefreshTokenCookie(Token token) {
		return token.createRefreshTokenCookie()
			.sameSite("None")
			.path("/")
			.secure(true)
			.httpOnly(true)
			.build();
	}
}
