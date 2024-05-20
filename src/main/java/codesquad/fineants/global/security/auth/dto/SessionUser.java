package codesquad.fineants.global.security.auth.dto;

import java.io.Serializable;

import codesquad.fineants.domain.member.domain.entity.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionUser implements Serializable {
	private String email;
	private String profileUrl;
	private String provider;

	public static SessionUser from(Member member) {
		return new SessionUser(member.getEmail(), member.getProfileUrl(), member.getProvider());
	}
}
