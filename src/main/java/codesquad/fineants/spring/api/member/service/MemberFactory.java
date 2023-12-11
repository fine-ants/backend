package codesquad.fineants.spring.api.member.service;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.spring.api.member.response.OauthUserProfile;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberFactory {

	private final NicknameGenerator generator;

	public Member createMember(OauthUserProfile profile) {
		return Member.builder()
			.email(profile.getEmail())
			.nickname(generator.generate())
			.provider(profile.getProvider())
			.profileUrl(profile.getProfileImage())
			.build();
	}
}
