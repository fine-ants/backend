package codesquad.fineants.global.security.auth.dto;

import codesquad.fineants.domain.member.domain.entity.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class MemberAuthentication {
	private Long id;
	private String email;
	private String nickname;
	private String provider;
	private String profileUrl;

	public static MemberAuthentication create(Long id, String email, String nickname, String provider,
		String profileUrl) {
		return new MemberAuthentication(id, email, nickname, provider, profileUrl);
	}

	public static MemberAuthentication from(Member member) {
		return new MemberAuthentication(
			member.getId(),
			member.getEmail(),
			member.getNickname(),
			member.getProvider(),
			member.getProfileUrl()
		);
	}
}
