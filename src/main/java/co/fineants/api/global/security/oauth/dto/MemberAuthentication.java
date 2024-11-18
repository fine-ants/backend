package co.fineants.api.global.security.oauth.dto;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberRole;
import co.fineants.api.domain.member.domain.entity.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"email", "nickname", "provider"})
public class MemberAuthentication {
	private Long id;
	private String email;
	private String nickname;
	private String provider;
	private String profileUrl;
	private Set<String> roleSet;

	public static MemberAuthentication create(Long id, String email, String nickname, String provider,
		String profileUrl, Set<String> roleSet) {
		return new MemberAuthentication(id, email, nickname, provider, profileUrl, roleSet);
	}

	public static MemberAuthentication from(Member member) {
		return new MemberAuthentication(
			member.getId(),
			member.getEmail(),
			member.getNickname(),
			member.getProvider(),
			member.getProfileUrl().orElse(null),
			member.getRoles().stream()
				.map(MemberRole::getRole)
				.map(Role::getRoleName)
				.collect(Collectors.toSet())
		);
	}

	public Set<SimpleGrantedAuthority> getSimpleGrantedAuthority() {
		return roleSet.stream()
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public String toString() {
		return String.format("MemberAuthentication(id=%d, nickname=%s, email=%s, roles=%s)", id, nickname, email,
			roleSet);
	}
}
