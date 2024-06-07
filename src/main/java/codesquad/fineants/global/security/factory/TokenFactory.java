package codesquad.fineants.global.security.factory;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import codesquad.fineants.global.security.oauth.dto.Token;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile(value = {"release", "!release"})
@Slf4j
public class TokenFactory {

	private final boolean secure;

	@PostConstruct
	public void init() {
		log.info("TokenFactory.secure={}", secure);
	}

	public TokenFactory(@Value("${token.secure:true}") boolean secure) {
		this.secure = secure;
	}

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
