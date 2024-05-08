package codesquad.fineants.global.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExchangeRateErrorCode implements ErrorCode {

	DUPLICATE_EXCHANGE_RATE(HttpStatus.CONFLICT, "이미 존재하는 환율입니다");

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
