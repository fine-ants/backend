package codesquad.fineants.domain.kis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KisConfig {

	public static final String baseUrl = "https://openapivts.koreainvestment.com:29443";
	public static final String realBaseUrl = "https://openapi.koreainvestment.com:9443";

	@Bean(name = "kisWebClient")
	public WebClient webClient() {
		return WebClient.builder()
			.baseUrl(baseUrl)
			.build();
	}

	@Bean(name = "realKisWebClient")
	public WebClient realWebClient() {
		return WebClient.builder()
			.baseUrl(realBaseUrl)
			.build();
	}
}
