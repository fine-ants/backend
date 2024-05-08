package codesquad.fineants.domain.member.domain.dto.request;

import javax.validation.constraints.NotBlank;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VerifyEmailRequest {
	@NotBlank(message = "이메일은 필수 정보입니다")
	private String email;
}
