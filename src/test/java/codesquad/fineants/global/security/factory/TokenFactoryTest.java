package codesquad.fineants.global.security.factory;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.global.security.oauth.dto.Token;
import jakarta.servlet.http.Cookie;

class TokenFactoryTest extends AbstractContainerBaseTest {

	@Autowired
	private TokenFactory tokenFactory;

	@DisplayName("액세스 토큰 값을 담은 쿠키 생성한다")
	@Test
	void createAccessTokenCookie() {
		// given
		Token token = Token.create("accessToken", "refreshToken");
		// when
		Cookie accessTokenCookie = tokenFactory.createAccessTokenCookie(token);
		// then
		Assertions.assertAll(
			() -> assertThat(accessTokenCookie.getSecure()).isTrue(),
			() -> assertThat(accessTokenCookie.isHttpOnly()).isTrue(),
			() -> assertThat(accessTokenCookie.getName()).isEqualTo("accessToken"),
			() -> assertThat(accessTokenCookie.getValue()).isEqualTo("accessToken")
		);
	}

	@DisplayName("리프레시 토큰 값을 담은 쿠키 생성한다")
	@Test
	void createRefreshTokenCookie() {
		// given
		Token token = Token.create("accessToken", "refreshToken");
		// when
		Cookie accessTokenCookie = tokenFactory.createRefreshTokenCookie(token);
		// then
		Assertions.assertAll(
			() -> assertThat(accessTokenCookie.getSecure()).isTrue(),
			() -> assertThat(accessTokenCookie.isHttpOnly()).isTrue(),
			() -> assertThat(accessTokenCookie.getName()).isEqualTo("refreshToken"),
			() -> assertThat(accessTokenCookie.getValue()).isEqualTo("refreshToken")
		);
	}
}
