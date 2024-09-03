package co.fineants.api.global.security.oauth.dto;

import java.io.Serializable;

import co.fineants.api.domain.member.domain.entity.Member;
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
