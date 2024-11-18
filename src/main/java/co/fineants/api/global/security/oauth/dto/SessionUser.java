package co.fineants.api.global.security.oauth.dto;

import java.io.Serializable;

import co.fineants.api.domain.member.domain.entity.Member;

public class SessionUser implements Serializable {
	private String email;
	private String profileUrl;
	private String provider;

	public SessionUser(String email, String profileUrl, String provider) {
		this.email = email;
		this.profileUrl = profileUrl;
		this.provider = provider;
	}

	public static SessionUser from(Member member) {
		return new SessionUser(member.getEmail(), member.getProfileUrl().orElse(null), member.getProvider());
	}

	@Override
	public String toString() {
		return String.format("SessionUser(email=%s, provider=%s, profileUrl=%s)", email, profileUrl, provider);
	}
}
