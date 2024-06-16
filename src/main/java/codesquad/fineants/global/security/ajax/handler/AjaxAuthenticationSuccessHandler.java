package codesquad.fineants.global.security.ajax.handler;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.domain.dto.response.LoginResponse;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.security.factory.TokenFactory;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.dto.Token;
import codesquad.fineants.global.security.oauth.service.TokenService;
import codesquad.fineants.global.success.MemberSuccessCode;
import codesquad.fineants.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AjaxAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	private final ObjectMapper objectMapper;
	private final TokenService tokenService;
	private final TokenFactory tokenFactory;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		Member member = (Member)authentication.getPrincipal();

		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("utf-8");

		Token token = tokenService.generateToken(MemberAuthentication.from(member), new Date());
		ApiResponse<LoginResponse> body = ApiResponse.success(MemberSuccessCode.OK_LOGIN);

		List<ResponseCookie> cookies = List.of(tokenFactory.createAccessTokenCookie(token),
			tokenFactory.createRefreshTokenCookie(token));
		String cookieValue = cookies.stream()
			.map(ResponseCookie::toString)
			.collect(Collectors.joining("; "));
		CookieUtils.setCookie(response, cookieValue);

		objectMapper.writeValue(response.getWriter(), body);
	}
}
