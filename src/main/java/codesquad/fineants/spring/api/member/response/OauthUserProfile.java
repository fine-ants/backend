package codesquad.fineants.spring.api.member.response;

import codesquad.fineants.domain.oauth.client.DecodedIdTokenPayload;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OauthUserProfile {

	private String email;
	private String profileImage;
	private String provider;

	public static OauthUserProfile from(DecodedIdTokenPayload payload, String provider) {
		return new OauthUserProfile(payload.getEmail(), payload.getPicture(), provider);
	}

	public static OauthUserProfile google(DecodedIdTokenPayload payload) {
		return new OauthUserProfile(payload.getEmail(), payload.getPicture(), "google");
	}

	public static OauthUserProfile kakao(DecodedIdTokenPayload payload) {
		return new OauthUserProfile(payload.getEmail(), payload.getPicture(), "kakao");
	}

	public static OauthUserProfile naver(String email, String picture) {
		return new OauthUserProfile(email, picture, "naver");
	}

	@Override
	public String toString() {
		return String.format("%s, %s(email=%s, profileImage=%s)", "유저 프로필 응답", this.getClass().getSimpleName(), email,
			profileImage);
	}
}
