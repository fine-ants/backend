package co.fineants.api.domain.member.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.member.domain.entity.MemberProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyCodeRequest {
	@NotBlank(message = "이메일은 필수 정보입니다")
	@Pattern(regexp = MemberProfile.EMAIL_REGEXP, message = "잘못된 입력 형식입니다")
	private final String email;
	@NotBlank(message = "검증코드는 필수 정보입니다")
	private final String code;

	@JsonCreator
	public VerifyCodeRequest(@JsonProperty("email") String email, @JsonProperty("code") String code) {
		this.email = email;
		this.code = code;
	}

	public String email() {
		return email;
	}

	public String code() {
		return code;
	}
}
