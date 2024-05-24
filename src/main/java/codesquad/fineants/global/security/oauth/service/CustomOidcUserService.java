package codesquad.fineants.global.security.oauth.service;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.domain.member.service.NicknameGenerator;
import codesquad.fineants.domain.notification_preference.repository.NotificationPreferenceRepository;
import codesquad.fineants.global.security.oauth.dto.OAuthAttribute;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomOidcUserService extends AbstractUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

	public CustomOidcUserService(MemberRepository memberRepository,
		NotificationPreferenceRepository notificationPreferenceRepository,
		NicknameGenerator nicknameGenerator, RoleRepository roleRepository) {
		super(memberRepository, notificationPreferenceRepository, nicknameGenerator, roleRepository);
	}

	@Override
	@Transactional
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		OAuthAttribute attributes = getUserInfo(userRequest, oAuth2User);

		Member member = saveOrUpdate(attributes);
		return (OidcUser)createOAuth2User(member, userRequest, attributes.getSub());
	}

	@Override
	OAuth2User createOAuth2User(Member member, OAuth2UserRequest userRequest, String sub) {
		Collection<? extends GrantedAuthority> authorities = member.getSimpleGrantedAuthorities();

		OidcIdToken idToken = ((OidcUserRequest)userRequest).getIdToken();
		Map<String, Object> claims = idToken.getClaims();
		Map<String, Object> memberAttribute = member.toMemberAttributeMap();
		memberAttribute.putAll(claims);

		OidcUserInfo userInfo = new OidcUserInfo(memberAttribute);

		String nameAttributeKey = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();
		memberAttribute.put(nameAttributeKey, sub);
		return new DefaultOidcUser(authorities, idToken, userInfo, nameAttributeKey);
	}
}
