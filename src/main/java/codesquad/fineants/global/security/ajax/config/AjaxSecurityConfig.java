package codesquad.fineants.global.security.ajax.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.service.MemberService;
import codesquad.fineants.global.security.ajax.entrypoint.CommonLoginAuthenticationEntryPoint;
import codesquad.fineants.global.security.ajax.filter.AjaxLoginProcessingFilter;
import codesquad.fineants.global.security.ajax.handler.AjaxAuthenticationFailHandler;
import codesquad.fineants.global.security.ajax.handler.AjaxAuthenticationSuccessHandler;
import codesquad.fineants.global.security.ajax.handler.AjaxLogoutHandler;
import codesquad.fineants.global.security.ajax.provider.AjaxAuthenticationProvider;
import codesquad.fineants.global.security.factory.TokenFactory;
import codesquad.fineants.global.security.handler.JwtLogoutSuccessHandler;
import codesquad.fineants.global.security.oauth.service.TokenService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AjaxSecurityConfig {
	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final ObjectMapper objectMapper;
	private final TokenService tokenService;
	private final MemberService memberService;
	private final TokenFactory tokenFactory;

	@Bean
	@Order(0)
	protected SecurityFilterChain ajaxSecurityFilterChain(HttpSecurity http) throws Exception {
		http.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http
			.securityMatcher(
				"/api/auth/login",
				"/api/auth/logout",
				"/error"
			)
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/api/auth/login").permitAll()
				.requestMatchers("/api/auth/logout").permitAll()
				.requestMatchers("/error").permitAll()
				.dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
				.anyRequest().authenticated()
			);
		http.authenticationProvider(authenticationProvider());
		http.authenticationManager(authenticationManager());

		AjaxLoginProcessingFilter ajaxLoginProcessingFilter = ajaxLoginProcessingFilter(authenticationManager(),
			objectMapper);
		ajaxLoginProcessingFilter.setAuthenticationSuccessHandler(ajaxAuthenticationSuccessHandler());
		ajaxLoginProcessingFilter.setAuthenticationFailureHandler(ajaxAuthenticationFailHandler());
		http.addFilterBefore(ajaxLoginProcessingFilter, UsernamePasswordAuthenticationFilter.class);

		http.logout(configurer -> configurer
			.logoutUrl("/api/auth/logout")
			.addLogoutHandler(logoutHandler())
			.logoutSuccessHandler(jwtLogoutSuccessHandler())
			.permitAll()
		);

		http.exceptionHandling(configurer ->
			configurer.authenticationEntryPoint(commonLoginAuthenticationEntryPoint()));
		http.csrf(AbstractHttpConfigurer::disable);
		return http.build();
	}

	@Bean
	protected AjaxLoginProcessingFilter ajaxLoginProcessingFilter(AuthenticationManager authenticationManager,
		ObjectMapper objectMapper) {
		return new AjaxLoginProcessingFilter(new AntPathRequestMatcher("/api/auth/login"),
			authenticationManager,
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
	protected LogoutHandler logoutHandler() {
		return new AjaxLogoutHandler(memberService);
	}

	@Bean
	protected JwtLogoutSuccessHandler jwtLogoutSuccessHandler() {
		return new JwtLogoutSuccessHandler(objectMapper);
	}

	@Bean
	protected CommonLoginAuthenticationEntryPoint commonLoginAuthenticationEntryPoint() {
		return new CommonLoginAuthenticationEntryPoint(objectMapper);
	}

	@Bean
	protected AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler() {
		return new AjaxAuthenticationSuccessHandler(objectMapper, tokenService, tokenFactory);
	}

	@Bean
	protected AjaxAuthenticationFailHandler ajaxAuthenticationFailHandler() {
		return new AjaxAuthenticationFailHandler(objectMapper);
	}
}
