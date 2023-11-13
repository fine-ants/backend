package codesquad.fineants.domain.oauth.repository;

import codesquad.fineants.domain.oauth.client.OauthClient;

public interface OauthClientRepository {
	OauthClient findOneBy(final String providerName);
}
