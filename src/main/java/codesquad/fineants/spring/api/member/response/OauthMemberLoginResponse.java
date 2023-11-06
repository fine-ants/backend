package codesquad.fineants.spring.api.member.response;

import codesquad.fineants.domain.jwt.Jwt;
import codesquad.fineants.domain.member.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthMemberLoginResponse {

	private Jwt jwt;
	private OauthMemberResponse user;

	public static OauthMemberLoginResponse of(Jwt jwt, Member member) {
		OauthMemberResponse user = OauthMemberResponse.from(member);
		return new OauthMemberLoginResponse(jwt, user);
	}

	@Override
	public String toString() {
		return String.format("%s, %s(member=%s)", "소셜 로그인 응답", this.getClass().getSimpleName(), user);
	}
}
