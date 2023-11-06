package codesquad.fineants.spring.api.member.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthMemberLogoutRequest {

	private String refreshToken;

	@Override
	public String toString() {
		return String.format("%s, %s(refreshToken=%s)", "로그아웃 요청", this.getClass().getSimpleName(), refreshToken);
	}
}
