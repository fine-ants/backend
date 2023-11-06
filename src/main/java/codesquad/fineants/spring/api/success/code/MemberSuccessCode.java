package codesquad.fineants.spring.api.success.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberSuccessCode implements SuccessCode {
	OK_MODIFIED_PROFILE_IMAGE(HttpStatus.OK, "프로필 사진이 수정되었습니다."),
	OK_MEMBER_TOWNS(HttpStatus.OK, "회원 동네 목록 조회를 완료하였습니다.");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "회원 성공 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
