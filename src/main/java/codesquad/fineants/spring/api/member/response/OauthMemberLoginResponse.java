package codesquad.fineants.spring.api.member.response;

import codesquad.fineants.domain.jwt.Jwt;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OauthMemberLoginResponse {

	private Jwt jwt;
	private OauthMemberResponse user;

	public static OauthMemberLoginResponse of(Jwt jwt, OauthMemberResponse oauthMemberResponse) {
		return new OauthMemberLoginResponse(jwt, oauthMemberResponse);
	}

	@Override
	public String toString() {
		return String.format("%s, %s(member=%s)", "소셜 로그인 응답", this.getClass().getSimpleName(), user);
	}
}
