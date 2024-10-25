package co.fineants.api.domain.member.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.member.domain.entity.MemberProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class VerifyEmailRequest {
	@NotBlank(message = "이메일은 필수 정보입니다")
	@Pattern(regexp = MemberProfile.EMAIL_REGEXP, message = "잘못된 입력 형식입니다")
	@JsonProperty
	private final String email;

	@JsonCreator
	public VerifyEmailRequest(@JsonProperty("email") String email) {
		this.email = email;
	}
}
