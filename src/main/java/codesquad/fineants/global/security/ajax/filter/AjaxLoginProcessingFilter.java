package codesquad.fineants.global.security.ajax.filter;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.domain.dto.request.LoginRequest;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.security.ajax.token.AjaxAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
		if (StringUtils.isEmpty(loginRequest.getEmail()) || StringUtils.isEmpty(loginRequest.getPassword())) {
			throw new FineAntsException(MemberErrorCode.LOGIN_FAIL);
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
