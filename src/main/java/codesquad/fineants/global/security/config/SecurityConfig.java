package codesquad.fineants.global.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.domain.member.service.NicknameGenerator;
import codesquad.fineants.global.security.auth.filter.JwtAuthFilter;
import codesquad.fineants.global.security.auth.handler.OAuth2SuccessHandler;
import codesquad.fineants.global.security.auth.handler.UserRequestMapper;
import codesquad.fineants.global.security.auth.service.CustomOAuth2UserService;
import codesquad.fineants.global.security.auth.service.CustomOidcUserService;
import codesquad.fineants.global.security.auth.service.TokenService;
import codesquad.fineants.global.security.filter.OAuth2AuthorizationRequestRedirectWithRedirectUrlParamFilter;
import jakarta.servlet.Filter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final MemberRepository memberRepository;
	private final TokenService tokenService;
	private final NicknameGenerator nicknameGenerator;
	private final RoleRepository roleRepository;
	private final UserRequestMapper userRequestMapper;
	private final String loginSuccessUri;

	public SecurityConfig(MemberRepository memberRepository, TokenService tokenService,
		NicknameGenerator nicknameGenerator, RoleRepository roleRepository, UserRequestMapper userRequestMapper,
		@Value("${oauth2.login-success-uri}") String loginSuccessUri) {
		this.memberRepository = memberRepository;
		this.tokenService = tokenService;
		this.nicknameGenerator = nicknameGenerator;
		this.roleRepository = roleRepository;
		this.userRequestMapper = userRequestMapper;
		this.loginSuccessUri = loginSuccessUri;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.sessionManagement(configurer -> configurer
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			);
		http.addFilterBefore(oAuth2AuthorizationRequestRedirectWithRedirectUrlParamFilter(),
			OAuth2AuthorizationRequestRedirectFilter.class);
		http.addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

		http
			.oauth2Login(configurer -> configurer
				.userInfoEndpoint(config -> config
					.userService(customOAuth2UserService())
					.oidcUserService(customOidcUserService())
				).successHandler(oAuth2SuccessHandler())
			);
		http.csrf(AbstractHttpConfigurer::disable);
		return http.build();
	}

	@Bean
	public Filter jwtAuthFilter() {
		return new JwtAuthFilter(tokenService);
	}

	@Bean
	public CustomOAuth2UserService customOAuth2UserService() {
		return new CustomOAuth2UserService(memberRepository, nicknameGenerator, roleRepository);
	}

	@Bean
	public CustomOidcUserService customOidcUserService() {
		return new CustomOidcUserService(memberRepository, nicknameGenerator, roleRepository);
	}

	@Bean
	public OAuth2SuccessHandler oAuth2SuccessHandler() {
		return new OAuth2SuccessHandler(tokenService, userRequestMapper, loginSuccessUri);
	}

	@Bean
	public WebSecurityCustomizer webSecurity() {
		return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}

	@Bean
	public OAuth2AuthorizationRequestRedirectWithRedirectUrlParamFilter oAuth2AuthorizationRequestRedirectWithRedirectUrlParamFilter() {
		return new OAuth2AuthorizationRequestRedirectWithRedirectUrlParamFilter();
	}
}
