package co.fineants.api.global.security.factory;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.security.oauth.dto.Token;

class TokenFactoryTest extends AbstractContainerBaseTest {

	@Autowired
	private TokenFactory tokenFactory;

	@DisplayName("액세스 토큰 값을 담은 쿠키 생성한다")
	@Test
	void createAccessTokenCookie() {
		// given
		Token token = Token.create("accessToken", "refreshToken");
		// when
		ResponseCookie accessTokenCookie = tokenFactory.createAccessTokenCookie(token);
		// then
		Assertions.assertAll(
			() -> assertThat(accessTokenCookie.isSecure()).isTrue(),
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
		ResponseCookie refreshTokenCookie = tokenFactory.createRefreshTokenCookie(token);
		// then
		Assertions.assertAll(
			() -> assertThat(refreshTokenCookie.isSecure()).isTrue(),
			() -> assertThat(refreshTokenCookie.isHttpOnly()).isTrue(),
			() -> assertThat(refreshTokenCookie.getName()).isEqualTo("refreshToken"),
			() -> assertThat(refreshTokenCookie.getValue()).isEqualTo("refreshToken")
		);
	}
}
