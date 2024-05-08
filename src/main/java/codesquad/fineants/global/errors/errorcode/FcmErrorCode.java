package codesquad.fineants.global.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FcmErrorCode implements ErrorCode {

	BAD_REQUEST_FCM_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 FCM 토큰입니다"),
	CONFLICT_FCM_TOKEN(HttpStatus.CONFLICT, "중복된 FCM 토큰입니다"),
	NOT_FOUND_FCM_TOKEN(HttpStatus.NOT_FOUND, "FCM 토큰을 찾을 수 없습니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "FCM 에러 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
