package codesquad.fineants.global.security.ajax.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.domain.dto.response.LoginResponse;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.dto.Token;
import codesquad.fineants.global.security.oauth.service.TokenService;
import codesquad.fineants.global.success.MemberSuccessCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AjaxAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	private final ObjectMapper objectMapper;
	private final TokenService tokenService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		Member member = (Member)authentication.getPrincipal();

		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("utf-8");

		Token token = tokenService.generateToken(MemberAuthentication.from(member));
		LoginResponse loginResponse = LoginResponse.from(token);
		ApiResponse<LoginResponse> body = ApiResponse.success(MemberSuccessCode.OK_LOGIN, loginResponse);
		objectMapper.writeValue(response.getWriter(), body);
	}
}
