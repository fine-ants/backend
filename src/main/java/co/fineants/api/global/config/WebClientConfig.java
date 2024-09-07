package co.fineants.api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
	@Bean
	public WebClient koreaInvestmentWebClient() {
		return WebClient.builder()
			.baseUrl("https://openapi.koreainvestment.com:9443")
			.build();
	}

	@Bean
	public WebClient localhostWebClient() {
		return WebClient.builder()
			.baseUrl("http://localhost:8080")
			.build();
	}
}
