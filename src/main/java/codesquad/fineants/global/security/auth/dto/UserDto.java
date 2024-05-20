package codesquad.fineants.global.security.auth.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class UserDto {
	private String email;
	private String nickname;
	private String provider;
	private String profileUrl;

	public static UserDto create(String email, String nickname, String provider, String profileUrl) {
		return new UserDto(email, nickname, provider, profileUrl);
	}
}
