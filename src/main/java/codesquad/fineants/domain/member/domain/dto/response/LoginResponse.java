package codesquad.fineants.domain.member.domain.dto.response;

import codesquad.fineants.domain.jwt.domain.Jwt;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class LoginResponse {
	private Jwt jwt;

	public static LoginResponse from(Jwt jwt) {
		return new LoginResponse(jwt);
	}
}
