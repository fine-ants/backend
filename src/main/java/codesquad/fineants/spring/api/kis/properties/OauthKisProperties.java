package codesquad.fineants.spring.api.kis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "oauth2.kis")
public class OauthKisProperties {

	private final String appkey;
	private final String secretkey;

	@ConstructorBinding
	public OauthKisProperties(String appkey, String secretkey) {
		this.appkey = appkey;
		this.secretkey = secretkey;
	}
}
