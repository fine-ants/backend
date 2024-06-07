package codesquad.fineants.global.security.factory;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import codesquad.fineants.global.security.oauth.dto.Token;
import jakarta.servlet.http.Cookie;
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

	public Cookie createAccessTokenCookie(Token token) {
		Cookie result = token.createAccessTokenCookie();
		result.setSecure(secure);
		result.setHttpOnly(true);
		return result;
	}

	public Cookie createRefreshTokenCookie(Token token) {
		Cookie result = token.createRefreshTokenCookie();
		result.setSecure(secure);
		result.setHttpOnly(true);
		return result;
	}
}
