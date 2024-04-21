package codesquad.fineants.spring.api.kis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "oauth2.kis")
public class OauthKisProperties {

	private final String appkey;
	private final String secretkey;
	private final String tokenURI;
	private final String currentPriceURI;
	private final String lastDayClosingPriceURI;
	private final String dividendURI;

	@ConstructorBinding
	public OauthKisProperties(String appkey, String secretkey, String tokenURI, String currentPriceURI,
		String lastDayClosingPriceURI, String dividendURI) {
		this.appkey = appkey;
		this.secretkey = secretkey;
		this.tokenURI = tokenURI;
		this.currentPriceURI = currentPriceURI;
		this.lastDayClosingPriceURI = lastDayClosingPriceURI;
		this.dividendURI = dividendURI;
	}
}
