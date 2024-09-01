package co.fineants.api.global.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.global.filter.SignupLoggingFilter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<SignupLoggingFilter> signupLoggingFilter() {
		FilterRegistrationBean<SignupLoggingFilter> filter = new FilterRegistrationBean<>();
		filter.setFilter(new SignupLoggingFilter());
		filter.addUrlPatterns("/api/auth/signup");
		return filter;
	}
}
