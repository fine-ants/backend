package codesquad.fineants.domain.member.domain.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class LoginRequest {
	private String email;
	private String password;

	public static LoginRequest create(String email, String password) {
		return new LoginRequest(email, password);
	}
}
