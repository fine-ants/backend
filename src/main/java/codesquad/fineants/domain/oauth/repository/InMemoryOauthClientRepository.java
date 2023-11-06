package codesquad.fineants.domain.oauth.repository;

import java.util.Map;

import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InMemoryOauthClientRepository implements OauthClientRepository {

	private final Map<String, OauthClient> oauthClientMap;

	@Override
	public OauthClient findOneBy(String providerName) {
		OauthClient oauthClient = oauthClientMap.get(providerName);
		if (oauthClient == null) {
			throw new NotFoundResourceException(OauthErrorCode.NOT_FOUND_PROVIDER);
		}
		return oauthClient;
	}
}
