package co.fineants.api.global.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisErrorCode implements ErrorCode {
	ACCESS_TOKEN_ISSUE_FAIL(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다");
	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "JWT 에러 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
