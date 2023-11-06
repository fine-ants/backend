package codesquad.fineants.spring.api.member.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OauthUserProfileResponse {

	private String email;
	private String profileImage;

	@Override
	public String toString() {
		return String.format("%s, %s(email=%s, profileImage=%s)", "유저 프로필 응답", this.getClass().getSimpleName(), email,
			profileImage);
	}
}
