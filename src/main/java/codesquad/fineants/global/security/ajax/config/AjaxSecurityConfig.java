package codesquad.fineants.global.security.ajax.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.global.security.ajax.entry_point.AjaxLoginAuthenticationEntryPoint;
import codesquad.fineants.global.security.ajax.filter.AjaxLoginProcessingFilter;
import codesquad.fineants.global.security.ajax.handler.AjaxAuthenticationFailHandler;
import codesquad.fineants.global.security.ajax.handler.AjaxAuthenticationSuccessHandler;
import codesquad.fineants.global.security.ajax.provider.AjaxAuthenticationProvider;
import codesquad.fineants.global.security.oauth.service.TokenService;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@Order(0)
public class AjaxSecurityConfig {
	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final ObjectMapper objectMapper;
	private final TokenService tokenService;

	@Bean
	protected SecurityFilterChain ajaxSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher(
				"/api/auth/login",
				"/api/auth/logout"
			)
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
				.requestMatchers("/api/auth/logout").permitAll()
				.anyRequest().authenticated()
			);
		http.authenticationProvider(authenticationProvider());
		http.authenticationManager(authenticationManager());

		AjaxLoginProcessingFilter ajaxLoginProcessingFilter = ajaxLoginProcessingFilter(authenticationManager(),
			objectMapper);
		ajaxLoginProcessingFilter.setAuthenticationSuccessHandler(ajaxAuthenticationSuccessHandler());
		ajaxLoginProcessingFilter.setAuthenticationFailureHandler(ajaxAuthenticationFailHandler());
		http.addFilterBefore(ajaxLoginProcessingFilter, UsernamePasswordAuthenticationFilter.class);

		http.exceptionHandling(configurer ->
			configurer.authenticationEntryPoint(ajaxLoginAuthenticationEntryPoint()));
		http.csrf(AbstractHttpConfigurer::disable);
		return http.build();
	}

	@Bean
	protected AjaxLoginProcessingFilter ajaxLoginProcessingFilter(AuthenticationManager authenticationManager,
		ObjectMapper objectMapper) {
		return new AjaxLoginProcessingFilter(new AntPathRequestMatcher("/api/auth/login"), authenticationManager,
			objectMapper);
	}

	@Bean
	protected AuthenticationManager authenticationManager() {
		return new ProviderManager(authenticationProvider());
	}

	@Bean
	protected AuthenticationProvider authenticationProvider() {
		return new AjaxAuthenticationProvider(userDetailsService, passwordEncoder);
	}

	@Bean
	protected AuthenticationEntryPoint ajaxLoginAuthenticationEntryPoint() {
		return new AjaxLoginAuthenticationEntryPoint(objectMapper);
	}

	@Bean
	protected AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler() {
		return new AjaxAuthenticationSuccessHandler(objectMapper, tokenService);
	}

	@Bean
	protected AjaxAuthenticationFailHandler ajaxAuthenticationFailHandler() {
		return new AjaxAuthenticationFailHandler(objectMapper);
	}
}
