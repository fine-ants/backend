package codesquad.fineants.domain.member.domain.dto.response;

import codesquad.fineants.domain.member.domain.entity.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "user")
@ToString
public class ProfileChangeResponse {
	private OauthMemberResponse user;

	public static ProfileChangeResponse from(Member member) {
		return new ProfileChangeResponse(OauthMemberResponse.from(member));
	}
}
