package co.fineants.api.global.errors.exception;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class UnAuthorizationException extends FineAntsException {

	public UnAuthorizationException(ErrorCode errorCode) {
		super(errorCode);
	}

	public UnAuthorizationException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
