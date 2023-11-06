package codesquad.fineants.spring.api.member.response;

import codesquad.fineants.domain.jwt.Jwt;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthMemberRefreshResponse {
	private String accessToken;

	public static OauthMemberRefreshResponse from(Jwt jwt) {
		return new OauthMemberRefreshResponse(jwt.getAccessToken());
	}

	@Override
	public String toString() {
		return String.format("%s, %s(accessToken=%s)", "액세스 토큰 갱신 응답", this.getClass().getSimpleName(),
			accessToken);
	}
}
