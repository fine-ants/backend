package co.fineants.api.global.errors.exception;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class FineAntsException extends RuntimeException {
	private final ErrorCode errorCode;

	public FineAntsException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public FineAntsException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}

	public int getHttpStatusCode() {
		return getErrorCode().getHttpStatus().value();
	}

	@Override
	public String toString() {
		return String.format("FineAntsException(errorCode=%s, message=%s)", errorCode, getMessage());
	}
}
