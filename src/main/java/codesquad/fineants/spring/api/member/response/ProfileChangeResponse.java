package codesquad.fineants.spring.api.member.response;

import codesquad.fineants.domain.member.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileChangeResponse {
	private OauthMemberResponse user;

	public static ProfileChangeResponse from(Member member) {
		return new ProfileChangeResponse(OauthMemberResponse.from(member));
	}
}
