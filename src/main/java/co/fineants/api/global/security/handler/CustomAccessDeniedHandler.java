package co.fineants.api.global.security.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import co.fineants.api.global.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException exception) throws IOException {
		ApiResponse<Object> body = ApiResponse.of(HttpStatus.FORBIDDEN, exception.getMessage(),
			exception.toString());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.getWriter().write(body.toString());
		response.getWriter().flush();
		response.getWriter().close();
	}
}
