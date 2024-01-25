package codesquad.fineants.spring.api.member.response;

import codesquad.fineants.domain.member.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "user")
@ToString
public class ProfileChangeResponse {
	private OauthMemberResponse user;

	public static ProfileChangeResponse from(Member member) {
		return new ProfileChangeResponse(OauthMemberResponse.from(member));
	}
}
