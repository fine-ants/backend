package codesquad.fineants.spring.api.member.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthAccessTokenResponse {

	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("scope")
	private String scope;
	@JsonProperty("token_type")
	private String tokenType;

	@Override
	public String toString() {
		return String.format("%s, %s(scope=%s, tokenType=%s)", "액세스 토큰 발급 응답", this.getClass().getSimpleName(), scope,
			tokenType);
	}
}
