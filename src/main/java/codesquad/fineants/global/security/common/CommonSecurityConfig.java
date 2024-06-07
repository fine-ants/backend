package codesquad.fineants.global.security.common;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class CommonSecurityConfig {

	@Bean
	public CorsConfiguration corsConfiguration() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(
			List.of("https://fineants.co", "https://release.fineants.co", "http://localhost:5173"));
		config.setAllowedMethods(Collections.singletonList("*"));
		config.setAllowedHeaders(Collections.singletonList("*"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);
		return config;
	}
}
