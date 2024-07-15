package codesquad.fineants.global.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PurchaseHistoryErrorCode implements ErrorCode {

	NOT_FOUND_PURCHASE_HISTORY(HttpStatus.NOT_FOUND, "매입 이력을 찾을 수 없습니다."),
	BAD_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력 형식입니다."),
	FORBIDDEN_PURCHASE_HISTORY(HttpStatus.FORBIDDEN, "매입 이력에 대한 권한이 없습니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "매입 이력 에러 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
