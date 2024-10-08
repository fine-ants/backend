package co.fineants.api.global.security.factory;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import co.fineants.api.global.security.oauth.dto.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenFactory {

	private final CookieDomainProvider provider;

	public ResponseCookie createAccessTokenCookie(Token token) {
		return token.createAccessTokenCookie()
			.domain(provider.domain())
			.sameSite("None")
			.path("/")
			.secure(true)
			.httpOnly(true)
			.build();
	}

	public ResponseCookie createRefreshTokenCookie(Token token) {
		return token.createRefreshTokenCookie()
			.domain(provider.domain())
			.sameSite("None")
			.path("/")
			.secure(true)
			.httpOnly(true)
			.build();
	}
}
