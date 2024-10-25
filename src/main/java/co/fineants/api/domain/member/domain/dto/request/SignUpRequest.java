package co.fineants.api.domain.member.domain.dto.request;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.member.domain.entity.MemberProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class SignUpRequest {
	@Pattern(regexp = MemberProfile.NICKNAME_REGEXP, message = "{nickname.notnull}")
	@NotBlank(message = "닉네임은 필수 정보입니다")
	@JsonProperty
	private final String nickname;

	@Pattern(regexp = MemberProfile.EMAIL_REGEXP, message = "잘못된 입력 형식입니다")
	@NotBlank(message = "이메일은 필수 정보입니다")
	@JsonProperty
	private final String email;

	@Pattern(regexp = MemberProfile.PASSWORD_REGEXP, message = "잘못된 입력 형식입니다")
	@NotBlank(message = "비밀번호는 필수 정보입니다")
	@JsonProperty
	private final String password;

	@Pattern(regexp = MemberProfile.PASSWORD_REGEXP, message = "잘못된 입력 형식입니다")
	@NotBlank(message = "비밀번호 확인은 필수 정보입니다")
	@JsonProperty
	private final String passwordConfirm;

	@JsonCreator
	public SignUpRequest(
		@JsonProperty("nickname") String nickname,
		@JsonProperty("email") String email,
		@JsonProperty("password") String password,
		@JsonProperty("passwordConfirm") String passwordConfirm) {
		this.nickname = nickname;
		this.email = email;
		this.password = password;
		this.passwordConfirm = passwordConfirm;
	}

	public SignUpServiceRequest toSignUpServiceRequest(MultipartFile profileImageFile) {
		return SignUpServiceRequest.create(nickname, email, password, passwordConfirm, profileImageFile);
	}

	@Override
	public String toString() {
		return String.format("SignUpRequest(nickname=%s, email=%s, password=%s, passwordConfirm=%s)", nickname, email,
			password, passwordConfirm);
	}
}
