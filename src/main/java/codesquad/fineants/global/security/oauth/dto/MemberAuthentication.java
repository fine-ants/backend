package codesquad.fineants.global.security.oauth.dto;

import java.util.Set;
import java.util.stream.Collectors;

import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.domain.entity.MemberRole;
import codesquad.fineants.domain.member.domain.entity.Role;
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
			member.getProfileUrl(),
			member.getRoles().stream()
				.map(MemberRole::getRole)
				.map(Role::getRoleName)
				.collect(Collectors.toSet())
		);
	}
}
