package co.fineants.support.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class TestSecurityConfig {

	@Bean
	@Order(1)
	protected SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher("/ws/test")
			.authorizeHttpRequests(authorize ->
				authorize.requestMatchers("/ws/test").permitAll()
					.anyRequest().authenticated()
			);
		return http.build();
	}
}
