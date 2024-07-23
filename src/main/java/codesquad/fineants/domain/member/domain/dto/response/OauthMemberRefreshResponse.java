package codesquad.fineants.domain.member.domain.dto.response;

import codesquad.fineants.global.security.oauth.dto.Token;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OauthMemberRefreshResponse {
	private String accessToken;

	public static OauthMemberRefreshResponse from(Token token) {
		return new OauthMemberRefreshResponse(token.getAccessToken());
	}

	@Override
	public String toString() {
		return String.format("%s, %s(accessToken=%s)", "액세스 토큰 갱신 응답", this.getClass().getSimpleName(),
			accessToken);
	}
}
