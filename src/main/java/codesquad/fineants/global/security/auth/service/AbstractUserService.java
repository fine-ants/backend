package codesquad.fineants.global.security.auth.service;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.domain.entity.MemberRole;
import codesquad.fineants.domain.member.domain.entity.Role;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.domain.member.service.NicknameGenerator;
import codesquad.fineants.domain.notification_preference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.notification_preference.repository.NotificationPreferenceRepository;
import codesquad.fineants.global.errors.errorcode.RoleErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.security.auth.dto.OAuthAttribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractUserService {

	private final MemberRepository memberRepository;
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final NicknameGenerator nicknameGenerator;
	private final RoleRepository roleRepository;

	OAuthAttribute getUserInfo(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
		String provider = userRequest.getClientRegistration().getRegistrationId();
		String nameAttributeKey = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();
		log.info("provider = {}", provider);
		log.info("nameAttributeKey = {}", nameAttributeKey);
		return OAuthAttribute.of(provider, oAuth2User.getAttributes(), nameAttributeKey);
	}

	Member saveOrUpdate(OAuthAttribute attributes) {
		Member member = attributes.getMemberFrom(memberRepository, nicknameGenerator);
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER")
			.orElseThrow(() -> new FineAntsException(RoleErrorCode.NOT_EXIST_ROLE));
		MemberRole memberRole = MemberRole.create(member, userRole);
		member.addMemberRole(memberRole);

		if (member.getNotificationPreference() == null) {
			NotificationPreference notificationPreference = NotificationPreference.defaultSetting(member);
			notificationPreferenceRepository.save(notificationPreference);
		}
		return memberRepository.save(member);
	}

	abstract OAuth2User createOAuth2User(Member member, OAuth2UserRequest userRequest, String sub);
}
