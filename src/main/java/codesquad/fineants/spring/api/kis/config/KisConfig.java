package codesquad.fineants.spring.api.kis.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import codesquad.fineants.spring.api.kis.properties.OauthKisProperties;

@EnableConfigurationProperties(value = OauthKisProperties.class)
@Configuration
public class KisConfig {

	public static final String baseUrl = "https://openapivts.koreainvestment.com:29443";

	@Bean(name = "kisWebClient")
	public WebClient webClient() {
		return WebClient.builder()
			.baseUrl(baseUrl)
			.build();
	}
}
