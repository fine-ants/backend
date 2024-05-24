package codesquad.fineants.global.security.ajax.entry_point;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AjaxLoginAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {

		MemberErrorCode errorCode = MemberErrorCode.LOGIN_FAIL;
		ApiResponse<String> body = ApiResponse.error(errorCode);
		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("utf-8");
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}
}
