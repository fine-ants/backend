package codesquad.fineants.global.security.oauth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import codesquad.fineants.domain.notification_preference.repository.NotificationPreferenceRepository;
import codesquad.fineants.global.security.handler.CustomAccessDeniedHandler;
import codesquad.fineants.global.security.oauth.filter.JwtAuthFilter;
import codesquad.fineants.global.security.oauth.filter.OAuth2AuthorizationRequestRedirectWithRedirectUrlParamFilter;
import codesquad.fineants.global.security.oauth.handler.OAuth2SuccessHandler;
import codesquad.fineants.global.security.oauth.handler.OAuth2UserMapper;
import codesquad.fineants.global.security.oauth.service.CustomOAuth2UserService;
import codesquad.fineants.global.security.oauth.service.CustomOidcUserService;
import codesquad.fineants.global.security.oauth.service.TokenService;
import jakarta.servlet.Filter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Order(value = 0)
public class OauthSecurityConfig {

	private final MemberRepository memberRepository;
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final TokenService tokenService;
	private final NicknameGenerator nicknameGenerator;
	private final RoleRepository roleRepository;
	private final OAuth2UserMapper OAuth2UserMapper;
	private final String loginSuccessUri;

	public OauthSecurityConfig(MemberRepository memberRepository,
		NotificationPreferenceRepository notificationPreferenceRepository, TokenService tokenService,
		NicknameGenerator nicknameGenerator, RoleRepository roleRepository, OAuth2UserMapper OAuth2UserMapper,
		@Value("${oauth2.login-success-uri}") String loginSuccessUri) {
		this.memberRepository = memberRepository;
		this.notificationPreferenceRepository = notificationPreferenceRepository;
		this.tokenService = tokenService;
		this.nicknameGenerator = nicknameGenerator;
		this.roleRepository = roleRepository;
		this.OAuth2UserMapper = OAuth2UserMapper;
		this.loginSuccessUri = loginSuccessUri;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorize ->
				authorize.requestMatchers(
						"/oauth2/authorization/**",
						"/login/oauth2/code/**",
						"/api/oauth/redirect",
						"/api/auth/google/login",
						"/api/auth/kakao/login",
						"/api/auth/naver/login",
						"/api/auth/login",
						"/api/auth/refresh/token"
					).permitAll()
					.anyRequest().authenticated()
			);
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
				)
				.successHandler(oAuth2SuccessHandler())
			);
		http.exceptionHandling(configurer ->
			configurer.accessDeniedHandler(customAccessDeniedHandler()));
		http.csrf(AbstractHttpConfigurer::disable);
		return http.build();
	}

	@Bean
	public Filter jwtAuthFilter() {
		return new JwtAuthFilter(tokenService);
	}

	@Bean
	public CustomOAuth2UserService customOAuth2UserService() {
		return new CustomOAuth2UserService(memberRepository, notificationPreferenceRepository, nicknameGenerator,
			roleRepository);
	}

	@Bean
	public CustomOidcUserService customOidcUserService() {
		return new CustomOidcUserService(memberRepository, notificationPreferenceRepository, nicknameGenerator,
			roleRepository);
	}

	@Bean
	public OAuth2SuccessHandler oAuth2SuccessHandler() {
		return new OAuth2SuccessHandler(tokenService, OAuth2UserMapper, loginSuccessUri);
	}

	@Bean
	public WebSecurityCustomizer webSecurity() {
		return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}

	@Bean
	public OAuth2AuthorizationRequestRedirectWithRedirectUrlParamFilter oAuth2AuthorizationRequestRedirectWithRedirectUrlParamFilter() {
		return new OAuth2AuthorizationRequestRedirectWithRedirectUrlParamFilter();
	}

	@Bean
	public CustomAccessDeniedHandler customAccessDeniedHandler() {
		return new CustomAccessDeniedHandler();
	}
}
