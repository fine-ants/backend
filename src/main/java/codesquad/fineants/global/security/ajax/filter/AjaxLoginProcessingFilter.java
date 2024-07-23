package codesquad.fineants.global.security.ajax.filter;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.domain.dto.request.LoginRequest;
import codesquad.fineants.global.security.ajax.token.AjaxAuthenticationToken;
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
		boolean notAjax = !isAjax(request);
		log.debug("notAjax : {}", notAjax);
		if (notAjax) {
			throw new IllegalStateException("Authentication is not supported");
		}

		LoginRequest loginRequest = objectMapper.readValue(request.getReader(), LoginRequest.class);
		log.debug("loginRequest : {}", loginRequest);
		if (!StringUtils.hasText(loginRequest.getEmail()) || !StringUtils.hasText(loginRequest.getPassword())) {
			throw new BadCredentialsException("Invalid email or password");
		}

		AbstractAuthenticationToken authRequest = AjaxAuthenticationToken.unauthenticated(loginRequest.getEmail(),
			loginRequest.getPassword());

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
