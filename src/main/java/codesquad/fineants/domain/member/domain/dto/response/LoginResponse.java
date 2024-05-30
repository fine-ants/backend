package codesquad.fineants.domain.member.domain.dto.response;

import codesquad.fineants.global.security.oauth.dto.Token;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class LoginResponse {
	private Token jwt;

	public static LoginResponse from(Token jwt) {
		return new LoginResponse(jwt);
	}
}
