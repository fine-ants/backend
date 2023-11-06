package codesquad.fineants.spring.api.member.response;

import codesquad.fineants.domain.member.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthMemberResponse {
	private Long id;
	private String nickname;
	private String email;
	private String profileUrl;

	public static OauthMemberResponse from(Member member) {
		return new OauthMemberResponse(member.getId(), member.getNickname(), member.getEmail(), member.getProfileUrl());
	}

	@Override
	public String toString() {
		return String.format("%s, %s(id=%d, nickname=%s, email=%s, profileUrl=%s)", "로그인 회원정보 응답",
			this.getClass().getSimpleName(), id, nickname, email, profileUrl);
	}
}
