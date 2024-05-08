package codesquad.fineants.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import codesquad.fineants.domain.oauth.properties.OauthProperties;
import codesquad.fineants.domain.oauth.repository.InMemoryOauthClientRepository;
import codesquad.fineants.domain.member.service.WebClientWrapper;
import lombok.RequiredArgsConstructor;

@EnableConfigurationProperties({OauthProperties.class})
@RequiredArgsConstructor
@Configuration
public class OauthConfig {

	private final OauthProperties oauthProperties;
	private final WebClientWrapper webClient;

	@Bean
	public InMemoryOauthClientRepository inMemoryOauthProviderRepository() {
		return new InMemoryOauthClientRepository(oauthProperties.createOauthClientMap(webClient));
	}
}
