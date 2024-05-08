package codesquad.fineants.global.errors.exception;

import codesquad.fineants.global.errors.errorcode.ErrorCode;

public class UnAuthorizationException extends FineAntsException {

	public UnAuthorizationException(ErrorCode errorCode) {
		super(errorCode);
	}

	public UnAuthorizationException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
