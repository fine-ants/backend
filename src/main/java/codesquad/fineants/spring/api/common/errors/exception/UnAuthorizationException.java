package codesquad.fineants.spring.api.common.errors.exception;

import codesquad.fineants.spring.api.common.errors.errorcode.ErrorCode;

public class UnAuthorizationException extends FineAntsException {

	public UnAuthorizationException(ErrorCode errorCode) {
		super(errorCode);
	}

	public UnAuthorizationException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
