package codesquad.fineants.domain.kis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "oauth2.kis")
public class KisProperties {

	private final KisProperty appkey;
	private final String secretkey;
	private final String tokenUrl;
	private final String currentPriceUrl;
	private final String closingPriceUrl;
	private final String dividendUrl;
	private final String ipoUrl;
	private final String searchStockInfoUrl;

	@ConstructorBinding
	public KisProperties(String appkey, String secretkey, String tokenUrl, String currentPriceUrl,
		String closingPriceUrl, String dividendUrl, String ipoUrl, String searchStockInfoUrl) {
		this.appkey = new KisProperty("appkey", appkey);
		this.secretkey = secretkey;
		this.tokenUrl = tokenUrl;
		this.currentPriceUrl = currentPriceUrl;
		this.closingPriceUrl = closingPriceUrl;
		this.dividendUrl = dividendUrl;
		this.ipoUrl = ipoUrl;
		this.searchStockInfoUrl = searchStockInfoUrl;
	}

	public String getAppKeyName() {
		return appkey.getName();
	}

	public String getAppKeyValue() {
		return appkey.getValue();
	}
}
