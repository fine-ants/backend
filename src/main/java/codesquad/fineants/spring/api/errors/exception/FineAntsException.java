package codesquad.fineants.spring.api.errors.exception;

import codesquad.fineants.spring.api.errors.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class FineAntsException extends RuntimeException {
	private final ErrorCode errorCode;
	private final String message;

	public FineAntsException(ErrorCode errorCode) {
		this(errorCode, errorCode.getMessage());
	}

	public FineAntsException(ErrorCode errorCode, String message) {
		this.errorCode = errorCode;
		this.message = message;
	}

	@Override
	public String toString() {
		return String.format("%s, %s(errorCode=%s, message=%s)", "중고거래 예외", this.getClass().getSimpleName(), errorCode,
			message);
	}
}
