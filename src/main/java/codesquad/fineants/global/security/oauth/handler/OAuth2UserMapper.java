package codesquad.fineants.global.security.oauth.handler;

import java.util.Set;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;

@Component
public class OAuth2UserMapper {

	public MemberAuthentication toMemberAuthentication(OAuth2User oAuth2User) {
		Long id = oAuth2User.getAttribute("id");
		String email = oAuth2User.getAttribute("email");
		String nickname = oAuth2User.getAttribute("nickname");
		String provider = oAuth2User.getAttribute("provider");
		String profileUrl = oAuth2User.getAttribute("profileUrl");
		Set<String> roleSet = oAuth2User.getAttribute("roleSet");
		return MemberAuthentication.create(id, email, nickname, provider, profileUrl, roleSet);
	}
}
