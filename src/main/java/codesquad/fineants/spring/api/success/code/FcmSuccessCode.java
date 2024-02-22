package codesquad.fineants.spring.api.success.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FcmSuccessCode implements SuccessCode {

	CREATED_FCM(HttpStatus.CREATED, "FCM 토큰을 성공적으로 등록하였습니다"),
	OK_DELETE_FCM(HttpStatus.OK, "FCM 토큰을 성공적으로 삭제하였습니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "FCM 성공 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
