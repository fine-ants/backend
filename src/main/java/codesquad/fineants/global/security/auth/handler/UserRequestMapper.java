package codesquad.fineants.global.security.auth.handler;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import codesquad.fineants.global.security.auth.dto.UserDto;

@Component
public class UserRequestMapper {

	public UserDto toDto(OAuth2User oAuth2User) {
		String email = oAuth2User.getAttribute("email");
		String nickname = oAuth2User.getAttribute("nickname");
		String provider = oAuth2User.getAttribute("provider");
		String profileUrl = oAuth2User.getAttribute("profileUrl");
		return UserDto.create(email, nickname, provider, profileUrl);
	}
}
