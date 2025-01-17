package co.fineants.api.global.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.global.filter.SignupLoggingFilter;
import co.fineants.api.global.filter.TraceIdFilter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<SignupLoggingFilter> signupLoggingFilter() {
		FilterRegistrationBean<SignupLoggingFilter> filter = new FilterRegistrationBean<>();
		filter.setFilter(new SignupLoggingFilter());
		filter.addUrlPatterns("/api/auth/signup");
		filter.setOrder(Integer.MIN_VALUE);
		return filter;
	}

	@Bean
	public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
		FilterRegistrationBean<TraceIdFilter> filter = new FilterRegistrationBean<>();
		filter.setFilter(new TraceIdFilter());
		filter.addUrlPatterns("/*");
		filter.setOrder(Integer.MIN_VALUE);
		return filter;
	}
}
