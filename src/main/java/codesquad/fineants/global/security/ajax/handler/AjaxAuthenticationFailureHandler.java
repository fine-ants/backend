package codesquad.fineants.global.security.ajax.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AjaxAuthenticationFailureHandler implements AuthenticationFailureHandler {
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		String errorMessage = "Invalid Username or Password";
		if (exception instanceof BadCredentialsException) {
			errorMessage = "Invalid Username or Password";
		} else if (exception instanceof InsufficientAuthenticationException) {
			errorMessage = "Invalid Secret Key";
		} else if (exception instanceof CredentialsExpiredException) {
			errorMessage = "Expired password";
		}

		ApiResponse<String> body = ApiResponse.error(MemberErrorCode.LOGIN_FAIL, errorMessage);
		objectMapper.writeValue(response.getWriter(), body);
	}
}
