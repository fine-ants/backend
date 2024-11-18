package co.fineants.api.global.security.factory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = {"release", "production"})
public class DeployCookieDomainProvider implements CookieDomainProvider {

	@Override
	public String domain() {
		return ".fineants.co";
	}
}
