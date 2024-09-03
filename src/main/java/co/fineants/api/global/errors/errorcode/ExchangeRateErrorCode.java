package co.fineants.api.global.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExchangeRateErrorCode implements ErrorCode {

	DUPLICATE_EXCHANGE_RATE(HttpStatus.CONFLICT, "이미 존재하는 통화입니다"),
	NOT_EXIST_EXCHANGE_RATE(HttpStatus.BAD_REQUEST, "존재하지 않는 통화입니다."),
	NOT_EXIST_BASE(HttpStatus.NOT_FOUND, "기본 통화가 존재하지 않습니다"),
	UNAVAILABLE_UPDATE_EXCHANGE_RATE(HttpStatus.BAD_REQUEST, "환율 업데이트가 불가능합니다"),
	UNAVAILABLE_DELETE_BASE_EXCHANGE_RATE(HttpStatus.BAD_REQUEST, "기준 통화 삭제가 불가능합니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "환율 에러 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
