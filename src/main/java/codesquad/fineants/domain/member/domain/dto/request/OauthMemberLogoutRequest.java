package codesquad.fineants.domain.member.domain.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthMemberLogoutRequest {

	private String refreshToken;

	public static OauthMemberLogoutRequest create(String refreshToken) {
		return new OauthMemberLogoutRequest(refreshToken);
	}

	@Override
	public String toString() {
		return String.format("%s, %s(refreshToken=%s)", "로그아웃 요청", this.getClass().getSimpleName(), refreshToken);
	}
}
