package codesquad.fineants.spring.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import codesquad.fineants.domain.oauth.properties.OauthProperties;
import codesquad.fineants.domain.oauth.repository.InMemoryOauthClientRepository;
import codesquad.fineants.spring.api.kis.properties.OauthKisProperties;
import lombok.RequiredArgsConstructor;

@EnableConfigurationProperties({OauthProperties.class, OauthKisProperties.class})
@RequiredArgsConstructor
@Configuration
public class OauthConfig {

	private final OauthProperties oauthProperties;
	private final OauthKisProperties oauthKisProperties;

	@Bean
	public InMemoryOauthClientRepository inMemoryOauthProviderRepository() {
		return new InMemoryOauthClientRepository(oauthProperties.createOauthClientMap());
	}
}
