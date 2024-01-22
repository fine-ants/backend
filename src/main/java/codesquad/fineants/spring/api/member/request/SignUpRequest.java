package codesquad.fineants.spring.api.member.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
	private String nickname;
	private String email;
	private String password;
	private String passwordConfirm;
}
