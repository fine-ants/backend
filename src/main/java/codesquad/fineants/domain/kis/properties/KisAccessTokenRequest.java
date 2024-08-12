package codesquad.fineants.domain.kis.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KisAccessTokenRequest {
	@JsonProperty("grant_type")
	private String grantType;
	private String appkey;
	private String secretkey;

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
