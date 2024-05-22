package codesquad.fineants.global.security.handler;

import static io.netty.handler.codec.http.HttpHeaders.Values.*;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import codesquad.fineants.global.api.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException exception) throws IOException, ServletException {
		ApiResponse<Object> body = ApiResponse.of(HttpStatus.FORBIDDEN, exception.getMessage(),
			exception.toString());
		response.setContentType(APPLICATION_JSON);
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.getWriter().write(body.toString());
		response.getWriter().flush();
		response.getWriter().close();
	}
}
