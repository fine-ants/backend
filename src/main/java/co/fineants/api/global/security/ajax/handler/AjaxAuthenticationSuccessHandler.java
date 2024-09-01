package co.fineants.api.global.security.ajax.handler;

import java.io.IOException;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.domain.member.domain.dto.response.LoginResponse;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.api.global.security.oauth.service.TokenService;
import co.fineants.api.global.success.MemberSuccessCode;
import co.fineants.api.global.util.CookieUtils;
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

		CookieUtils.setCookie(response, tokenFactory.createAccessTokenCookie(token));
		CookieUtils.setCookie(response, tokenFactory.createRefreshTokenCookie(token));

		objectMapper.writeValue(response.getWriter(), body);
	}
}
