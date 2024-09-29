package co.fineants.api.domain.member.domain.entity;

import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"email", "nickname", "provider"}, callSuper = false)
public class MemberProfile {
	@Column(name = "email", nullable = false)
	private String email;
	@Column(name = "nickname", unique = true, nullable = false)
	private String nickname;
	@Column(name = "provider", nullable = false)
	private String provider;
	@Column(name = "password")
	private String password;

	public static MemberProfile localMemberProfile(String email, String nickname, String password) {
		return new MemberProfile(email, nickname, "local", password);
	}

	public static MemberProfile oauthMemberProfile(String email, String nickname, String provider) {
		return new MemberProfile(email, nickname, provider, null);
	}

	public void changePassword(String password) {
		this.password = password;
	}

	public void changeNickname(String nickname) {
		this.nickname = nickname;
	}

	public Map<String, Object> toMap() {
		return Map.ofEntries(
			Map.entry("email", email),
			Map.entry("nickname", nickname),
			Map.entry("provider", provider)
		);
	}
}
