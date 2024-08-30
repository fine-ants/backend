package co.fineants.api.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.member.domain.dto.response.OauthMemberLoginResponse;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.api.global.success.MemberSuccessCode;
import co.fineants.api.global.util.CookieUtils;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OauthRestController {

	private final TokenFactory tokenFactory;

	@GetMapping("/api/oauth/redirect")
	@PermitAll
	public ApiResponse<OauthMemberLoginResponse> oauthRedirect(
		HttpServletRequest servletRequest,
		HttpServletResponse servletResponse) {
		String accessToken = CookieUtils.getAccessToken(servletRequest);
		String refreshToken = CookieUtils.getRefreshToken(servletRequest);
		Token token = Token.create(accessToken, refreshToken);
		OauthMemberLoginResponse response = OauthMemberLoginResponse.of(token);

		CookieUtils.setCookie(servletResponse, tokenFactory.createAccessTokenCookie(token));
		CookieUtils.setCookie(servletResponse, tokenFactory.createRefreshTokenCookie(token));

		return ApiResponse.success(MemberSuccessCode.OK_LOGIN, response);
	}
}
