package codesquad.fineants.spring.api.member.response;

import codesquad.fineants.domain.jwt.Jwt;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginResponse {
	private Jwt jwt;
	private OauthMemberResponse user;

	public static LoginResponse from(Jwt jwt, OauthMemberResponse user) {
		return new LoginResponse(jwt, user);
	}
}
