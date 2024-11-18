package co.fineants.api.global.security.factory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = {"local", "test"})
public class LocalCookieDomainProvider implements CookieDomainProvider {
	@Override
	public String domain() {
		return null;
	}
}
