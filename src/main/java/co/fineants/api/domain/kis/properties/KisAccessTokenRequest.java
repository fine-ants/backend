package co.fineants.api.domain.kis.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.ToString;

@ToString
public class KisAccessTokenRequest {
	@JsonProperty("grant_type")
	private final String grantType;
	@JsonProperty("appkey")
	private final String appkey;
	@JsonProperty("appsecret")
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
