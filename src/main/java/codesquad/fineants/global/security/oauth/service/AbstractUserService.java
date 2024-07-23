package codesquad.fineants.global.security.oauth.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.domain.entity.MemberRole;
import codesquad.fineants.domain.member.domain.entity.Role;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.domain.member.service.NicknameGenerator;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.notificationpreference.repository.NotificationPreferenceRepository;
import codesquad.fineants.global.security.oauth.dto.OAuthAttribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractUserService {
	private static final String DEFAULT_ROLE = "ROLE_USER";
	private final MemberRepository memberRepository;
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final NicknameGenerator nicknameGenerator;
	private final RoleRepository roleRepository;

	public OAuthAttribute getUserInfo(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
		String provider = userRequest.getClientRegistration().getRegistrationId();
		String nameAttributeKey = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();
		log.info("provider = {}", provider);
		log.info("nameAttributeKey = {}", nameAttributeKey);
		return OAuthAttribute.of(provider, oAuth2User.getAttributes(), nameAttributeKey);
	}

	public Member saveOrUpdate(OAuthAttribute attributes) {
		Member member = attributes.getMemberFrom(memberRepository)
			.orElseGet(() -> attributes.toEntity(nicknameGenerator));
		Set<String> roleNames = member.getRoles().stream()
			.map(MemberRole::getRoleName)
			.collect(Collectors.toSet());
		if (roleNames.isEmpty()) {
			roleNames.add(DEFAULT_ROLE);
		}

		Set<Role> findRoles = roleRepository.findRolesByRoleNames(roleNames);
		Set<MemberRole> memberRoles = findRoles.stream()
			.map(r -> MemberRole.create(member, r))
			.collect(Collectors.toSet());
		member.addMemberRole(memberRoles);

		if (member.getNotificationPreference() == null) {
			NotificationPreference notificationPreference = NotificationPreference.defaultSetting(member);
			notificationPreferenceRepository.save(notificationPreference);
		}
		return memberRepository.save(member);
	}

	abstract OAuth2User createOAuth2User(Member member, OAuth2UserRequest userRequest, String sub);
}
