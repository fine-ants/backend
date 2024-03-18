package codesquad.fineants.spring.api.member.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SignUpRequest {
	@Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "잘못된 입력 형식입니다")
	@NotBlank(message = "닉네임은 필수 정보입니다")
	private String nickname;
	@Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "잘못된 입력 형식입니다")
	@NotBlank(message = "이메일은 필수 정보입니다")
	private String email;
	@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,16}$", message = "잘못된 입력 형식입니다")
	@NotBlank(message = "비밀번호는 필수 정보입니다")
	private String password;
	@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,16}$", message = "잘못된 입력 형식입니다")
	@NotBlank(message = "비밀번호 확인은 필수 정보입니다")
	private String passwordConfirm;
}
