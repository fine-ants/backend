package co.fineants.api.global.security.oauth.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberRole;
import co.fineants.api.domain.member.domain.entity.Role;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.member.service.NicknameGenerator;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.notificationpreference.repository.NotificationPreferenceRepository;
import co.fineants.api.global.security.oauth.dto.OAuthAttribute;
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
		attributes.updateProfileUrlIfAbsent(member);

		Set<String> roleNames = member.getRoles().stream()
			.map(MemberRole::getRoleName)
			.collect(Collectors.toSet());
		if (roleNames.isEmpty()) {
			roleNames.add(DEFAULT_ROLE);
		}

		Set<Role> findRoles = roleRepository.findRolesByRoleNames(roleNames);
		MemberRole[] memberRoles = findRoles.stream()
			.map(r -> MemberRole.create(member, r))
			.toArray(MemberRole[]::new);
		member.addMemberRole(memberRoles);

		if (member.getNotificationPreference() == null) {
			NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
			notificationPreferenceRepository.save(notificationPreference);
		}
		return memberRepository.save(member);
	}

	abstract OAuth2User createOAuth2User(Member member, OAuth2UserRequest userRequest, String sub);
}
