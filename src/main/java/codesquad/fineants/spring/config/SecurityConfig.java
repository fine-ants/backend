package codesquad.fineants.spring.config;

import codesquad.fineants.domain.jwt.JwtProvider;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthenticationContext;
import codesquad.fineants.spring.api.member.service.OauthMemberRedisService;
import codesquad.fineants.spring.filter.CorsFilter;
import codesquad.fineants.spring.filter.JwtAuthorizationFilter;
import codesquad.fineants.spring.filter.LogoutFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final AuthenticationContext authenticationContext;
    private final ObjectMapper objectMapper;
    private final OauthMemberRedisService redisService;
    private final MemberRepository memberRepository;

    // 인증이 필요하지 않은 주소
    private final String[] accessUrl = {"/api/auth/login",
            "/api/auth/signup",
            "/api/auth/**/authUrl",
            "/api/auth/**/login",
            "/api/auth/refresh/token",
            "/api/auth/logout",
            "/api/stocks/**"};
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /**
         * 기본 설정
         */
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션을 사용하지 않는다.
        http.formLogin().disable(); // 로그인 UI Form 미사용
        http.httpBasic().disable(); // http basic 방식 미사용 (ID+PW로 인증), bearer token 방식 사용 (JWT 토큰)

        /**
         * 필터 추가
         */
        http.addFilterBefore(new CorsFilter(), UsernamePasswordAuthenticationFilter.class); // cors 필터
        http.addFilterBefore(new JwtAuthorizationFilter(jwtProvider, authenticationContext, objectMapper, redisService,memberRepository), UsernamePasswordAuthenticationFilter.class); // JWT 토큰으로 모든 접근에 대해 인증한다.
        http.addFilterBefore(new LogoutFilter(redisService, objectMapper), UsernamePasswordAuthenticationFilter.class);

        /**
         * 요청 허용 / 미허용
         */
        http.authorizeRequests()
                .antMatchers(accessUrl).permitAll()
                .anyRequest().authenticated();

        return http.build();
    }
}
