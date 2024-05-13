package codesquad.fineants.global.filter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.service.OauthMemberRedisService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.errors.errorcode.ErrorCode;
import codesquad.fineants.global.errors.errorcode.JwtErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.errors.exception.UnAuthorizationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LogoutFilter extends OncePerRequestFilter {

	private final OauthMemberRedisService redisService;
	private final ObjectMapper objectMapper;

	private static final AntPathMatcher pathMatcher = new AntPathMatcher();
	private static final List<String> excludeUrlPatterns = List.of("/api/**");

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return excludeUrlPatterns.stream()
			.anyMatch(pattern -> pathMatcher.match(pattern, request.getRequestURI()))
			&& !pathMatcher.match("/api/auth/logout", request.getRequestURI());
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
			redisService.validateAlreadyLogout(token);
		} catch (FineAntsException e) {
			setErrorResponse(response, e.getErrorCode());
			return;
		}

		filterChain.doFilter(request, response);
	}

	private Optional<String> extractJwt(HttpServletRequest request) {
		String header = request.getHeader(JwtAuthorizationFilter.AUTHORIZATION);

		if (!StringUtils.hasText(header) || !header.startsWith(JwtAuthorizationFilter.BEARER)) {
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
