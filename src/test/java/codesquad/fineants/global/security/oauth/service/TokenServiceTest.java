package codesquad.fineants.global.security.oauth.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.dto.Token;

@ActiveProfiles("test")
class TokenServiceTest {

	private TokenService tokenService;

	@BeforeEach
	void setUp() {
		String secretKey = "awoeifjoawiejfaweifoajwefiojaweofjwaoefjaowejfoawiefjaweofjawef";
		long tokenExpirationMiliseconds = 300000;
		long refreshTokenExpirationMiliseconds = 36000000;
		tokenService = new TokenService(secretKey, tokenExpirationMiliseconds, refreshTokenExpirationMiliseconds);
	}

	@DisplayName("액세스 토큰 및 리프레시 토큰을 생성한다")
	@Test
	void generateToken() {
		// given
		MemberAuthentication authentication = createMemberAuthentication();
		// when
		Token token = tokenService.generateToken(authentication);

		// then
		assertAll(
			() -> assertThat(token).isNotNull(),
			() -> assertThat(token)
				.extracting(Token::getAccessToken, Token::getRefreshToken)
				.doesNotContainNull()
		);
	}

	@DisplayName("토큰을 검증한다")
	@Test
	void verifyToken() {
		// given
		MemberAuthentication authentication = createMemberAuthentication();
		Token token = tokenService.generateToken(authentication);
		// when
		boolean actual1 = tokenService.verifyToken(token.getAccessToken());
		boolean actual2 = tokenService.verifyToken(token.getRefreshToken());
		// then
		assertAll(
			() -> assertThat(actual1).isTrue(),
			() -> assertThat(actual2).isTrue()
		);
	}

	@DisplayName("토큰을 파싱한다")
	@Test
	void parseMemberAuthenticationToken() {
		// given
		MemberAuthentication authentication = createMemberAuthentication();
		Token token = tokenService.generateToken(authentication);
		// when
		MemberAuthentication memberAuthentication = tokenService.parseMemberAuthenticationToken(token.getAccessToken());

		// then
		assertAll(
			() -> assertThat(memberAuthentication)
				.extracting(MemberAuthentication::getId, MemberAuthentication::getEmail,
					MemberAuthentication::getNickname, MemberAuthentication::getProvider,
					MemberAuthentication::getProfileUrl, MemberAuthentication::getRoleSet)
				.containsExactly(1L, "user1@gmail.com", "ant1111", "local", "profileUrl", Set.of("ROLE_USER"))
		);
	}

	@DisplayName("액세스 토큰을 갱신한다")
	@Test
	void refreshToken() {
		// given
		MemberAuthentication authentication = createMemberAuthentication();
		Token token = tokenService.generateToken(authentication);
		// when
		Token newToken = tokenService.refreshToken(token.getRefreshToken(), LocalDateTime.now());
		// then
		assertThat(newToken).isNotNull();
	}

	@NotNull
	private MemberAuthentication createMemberAuthentication() {
		return MemberAuthentication.create(
			1L,
			"user1@gmail.com",
			"ant1111",
			"local",
			"profileUrl",
			Set.of("ROLE_USER")
		);
	}
}
