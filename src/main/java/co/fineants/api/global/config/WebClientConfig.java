package co.fineants.api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
	@Bean("koreaInvestmentWebClient")
	public WebClient koreaInvestmentWebClient() {
		return WebClient.builder()
			.baseUrl("https://openapi.koreainvestment.com:9443")
			.build();
	}
}
