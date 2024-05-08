package codesquad.fineants.global.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.jwt.domain.JwtProvider;
import codesquad.fineants.domain.oauth.support.AuthenticationContext;
import codesquad.fineants.domain.member.service.OauthMemberRedisService;
import codesquad.fineants.global.filter.JwtAuthorizationFilter;
import codesquad.fineants.global.filter.LogoutFilter;
import codesquad.fineants.global.filter.SignupLoggingFilter;
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

	@Bean
	public FilterRegistrationBean<SignupLoggingFilter> signupLoggingFilter() {
		FilterRegistrationBean<SignupLoggingFilter> filter = new FilterRegistrationBean<>();
		filter.setFilter(new SignupLoggingFilter());
		filter.addUrlPatterns("/api/auth/signup");
		return filter;
	}
}
