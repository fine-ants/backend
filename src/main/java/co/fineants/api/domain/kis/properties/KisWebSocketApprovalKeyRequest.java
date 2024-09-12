package co.fineants.api.domain.kis.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.ToString;

@ToString
public class KisWebSocketApprovalKeyRequest {
	@JsonProperty("grant_type")
	private final String grantType;
	@JsonProperty("appkey")
	private final String appkey;
	@JsonProperty("secretkey")
	private final String secretkey;

	private KisWebSocketApprovalKeyRequest(String appkey, String secretkey) {
		this.grantType = "client_credentials";
		this.appkey = appkey;
		this.secretkey = secretkey;
	}

	public static KisWebSocketApprovalKeyRequest create(KisProperties kisProperties) {
		return new KisWebSocketApprovalKeyRequest(
			kisProperties.getAppkey(),
			kisProperties.getSecretkey());
	}
}
