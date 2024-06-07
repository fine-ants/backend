package codesquad.fineants.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.member.domain.dto.response.OauthMemberLoginResponse;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.security.factory.TokenFactory;
import codesquad.fineants.global.security.oauth.dto.Token;
import codesquad.fineants.global.success.MemberSuccessCode;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OauthRestController {

	private final TokenFactory tokenFactory;

	@GetMapping("/api/oauth/redirect")
	@PermitAll
	public ApiResponse<OauthMemberLoginResponse> oauthRedirect(
		@RequestParam String accessToken,
		@RequestParam String refreshToken,
		HttpServletResponse servletResponse) {
		Token token = Token.create(accessToken, refreshToken);
		OauthMemberLoginResponse response = OauthMemberLoginResponse.of(token);

		servletResponse.addCookie(tokenFactory.createAccessTokenCookie(token));
		servletResponse.addCookie(tokenFactory.createRefreshTokenCookie(token));

		return ApiResponse.success(MemberSuccessCode.OK_LOGIN, response);
	}
}
