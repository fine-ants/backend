package codesquad.fineants.domain.kis.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.ToString;

@ToString
public class KisAccessTokenRequest {
	@JsonProperty("grant_type")
	private final String grantType;
	private final String appkey;
	private final String secretkey;

	private KisAccessTokenRequest(String appkey, String secretkey) {
		this.grantType = "client_credentials";
		this.appkey = appkey;
		this.secretkey = secretkey;
	}

	public static KisAccessTokenRequest create(KisProperties kisProperties) {
		return new KisAccessTokenRequest(
			kisProperties.getAppkey(),
			kisProperties.getSecretkey());
	}
}
