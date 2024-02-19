package codesquad.fineants.spring.api.member.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthToken {

	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("scope")
	private String scope;
	@JsonProperty("token_type")
	private String tokenType;
	@JsonProperty("id_token")
	private String idToken;

	@Override
	public String toString() {
		return String.format("%s, %s(scope=%s, tokenType=%s, idToken=%s)", "액세스 토큰 발급 응답",
			this.getClass().getSimpleName(), scope,
			tokenType, idToken);
	}

	public String createAuthorizationHeader() {
		return String.join(" ", tokenType, accessToken);
	}

	public boolean isEmpty() {
		return accessToken == null && idToken == null;
	}
}
