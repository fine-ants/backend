package codesquad.fineants.global.security.ajax.entrypoint;

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
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CommonLoginAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {

		log.error("AuthenticationEntryPoint : {}", exception.getMessage());
		MemberErrorCode errorCode = MemberErrorCode.UNAUTHORIZED_MEMBER;
		ApiResponse<String> body = ApiResponse.error(errorCode);
		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("utf-8");
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}
}
