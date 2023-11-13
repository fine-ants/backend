package codesquad.fineants.spring.filter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.jwt.JwtProvider;
import codesquad.fineants.domain.oauth.support.AuthenticationContext;
import codesquad.fineants.spring.api.errors.errorcode.ErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.JwtErrorCode;
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.errors.exception.UnAuthorizationException;
import codesquad.fineants.spring.api.member.service.OauthMemberRedisService;
import codesquad.fineants.spring.api.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	public static final String AUTHORIZATION = "Authorization";
	public static final String BEARER = "Bearer";
	private static final AntPathMatcher pathMatcher = new AntPathMatcher();
	private static final List<String> excludeUrlPatterns = List.of(
		"/api/auth/**/authUrl",
		"/api/auth/**/login",
		"/api/auth/refresh/token",
		"/api/auth/logout",
		"/api/stocks/**");
	private final JwtProvider jwtProvider;
	private final AuthenticationContext authenticationContext;
	private final ObjectMapper objectMapper;
	private final OauthMemberRedisService redisService;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return excludeUrlPatterns.stream()
			.anyMatch(pattern -> pathMatcher.match(pattern, request.getRequestURI()));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		if (CorsUtils.isPreFlightRequest(request)) {
			filterChain.doFilter(request, response);
			return;
		}
		try {
			String token = extractJwt(request).orElseThrow(
				() -> new UnAuthorizationException(JwtErrorCode.EMPTY_TOKEN));
			jwtProvider.validateToken(token);
			redisService.validateAlreadyLogout(token);
			authenticationContext.setAuthMember(jwtProvider.extractAuthMember(token));
		} catch (FineAntsException e) {
			setErrorResponse(response, e.getErrorCode());
			return;
		}

		filterChain.doFilter(request, response);
	}

	private Optional<String> extractJwt(HttpServletRequest request) {
		String header = request.getHeader(AUTHORIZATION);

		if (!StringUtils.hasText(header) || !header.startsWith(BEARER)) {
			return Optional.empty();
		}

		return Optional.of(header.split(" ")[1]);
	}

	private void setErrorResponse(HttpServletResponse httpServletResponse, ErrorCode errorCode) throws IOException {
		httpServletResponse.setStatus(errorCode.getHttpStatus().value());
		httpServletResponse.setContentType("application/json");
		httpServletResponse.setCharacterEncoding("UTF-8");
		ApiResponse<Object> body = ApiResponse.error(errorCode);
		httpServletResponse.getWriter().write(objectMapper.writeValueAsString(body));
	}
}
