package codesquad.fineants.domain.kis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KisConfig {
	
	@Bean(name = "realKisWebClient")
	public WebClient realWebClient() {
		return WebClient.builder()
			.baseUrl("https://openapi.koreainvestment.com:9443")
			.build();
	}
}
