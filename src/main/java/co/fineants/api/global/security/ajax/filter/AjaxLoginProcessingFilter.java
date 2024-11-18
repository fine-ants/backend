package co.fineants.api.global.security.ajax.filter;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.domain.member.domain.dto.request.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AjaxLoginProcessingFilter extends AbstractAuthenticationProcessingFilter {

	private final ObjectMapper objectMapper;

	public AjaxLoginProcessingFilter(RequestMatcher loginRequestMatcher, AuthenticationManager authenticationManager,
		ObjectMapper objectMapper) {
		super(loginRequestMatcher, authenticationManager);
		this.objectMapper = objectMapper;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException,
		IOException {
		if (!isAjax(request)) {
			throw new IllegalStateException("Authentication is not supported");
		}

		LoginRequest loginRequest = objectMapper.readValue(request.getReader(), LoginRequest.class);
		if (!loginRequest.hasEmail() || !loginRequest.hasPassword()) {
			throw new BadCredentialsException(String.format("Invalid email or password, %s", loginRequest));
		}
		AbstractAuthenticationToken authRequest = loginRequest.toUnauthenticatedAjaxToken();

		setDetails(request, authRequest);
		return getAuthenticationManager().authenticate(authRequest);
	}

	private boolean isAjax(HttpServletRequest request) {
		return MediaType.APPLICATION_JSON_VALUE.equals(request.getHeader("Content-Type"));
	}

	private void setDetails(HttpServletRequest request, AbstractAuthenticationToken authRequest) {
		authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
	}
}
