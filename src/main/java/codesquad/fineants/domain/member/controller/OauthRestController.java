package codesquad.fineants.domain.member.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.member.domain.dto.response.OauthMemberLoginResponse;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.security.factory.TokenFactory;
import codesquad.fineants.global.security.oauth.dto.Token;
import codesquad.fineants.global.success.MemberSuccessCode;
import codesquad.fineants.global.util.CookieUtils;
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

		List<ResponseCookie> cookies = List.of(tokenFactory.createAccessTokenCookie(token),
			tokenFactory.createRefreshTokenCookie(token));
		String cookieValue = cookies.stream()
			.map(ResponseCookie::toString)
			.collect(Collectors.joining("; "));
		CookieUtils.setCookie(servletResponse, cookieValue);

		return ApiResponse.success(MemberSuccessCode.OK_LOGIN, response);
	}
}
