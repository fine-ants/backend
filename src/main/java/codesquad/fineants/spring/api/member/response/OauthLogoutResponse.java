package codesquad.fineants.spring.api.member.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthLogoutResponse {

	private String email;

	@Override
	public String toString() {
		return String.format("%s, %s(email=%s)", "로그아웃 응답", this.getClass().getSimpleName(), email);
	}
}
