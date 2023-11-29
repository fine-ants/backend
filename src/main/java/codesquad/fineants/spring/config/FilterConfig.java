package codesquad.fineants.spring.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.jwt.JwtProvider;
import codesquad.fineants.domain.oauth.support.AuthenticationContext;
import codesquad.fineants.spring.api.member.service.OauthMemberRedisService;
import codesquad.fineants.spring.filter.JwtAuthorizationFilter;
import codesquad.fineants.spring.filter.LogoutFilter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class FilterConfig {

	private final JwtProvider jwtProvider;
	private final AuthenticationContext authenticationContext;
	private final ObjectMapper objectMapper;
	private final OauthMemberRedisService redisService;

	@Bean
	public FilterRegistrationBean<JwtAuthorizationFilter> jwtAuthorizationFilter() {
		FilterRegistrationBean<JwtAuthorizationFilter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
		filterFilterRegistrationBean.setFilter(
			new JwtAuthorizationFilter(jwtProvider, authenticationContext, objectMapper, redisService));
		filterFilterRegistrationBean.addUrlPatterns("/api/*");
		return filterFilterRegistrationBean;
	}

	@Bean
	public FilterRegistrationBean<LogoutFilter> logoutFiler() {
		FilterRegistrationBean<LogoutFilter> logoutFilerBean = new FilterRegistrationBean<>();
		logoutFilerBean.setFilter(new LogoutFilter(redisService, objectMapper));
		logoutFilerBean.addUrlPatterns("/api/auth/logout");
		return logoutFilerBean;
	}
}
