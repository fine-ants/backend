package codesquad.fineants.spring.api.common.errors.exception;

import codesquad.fineants.spring.api.common.errors.errorcode.ErrorCode;

public class ConflictException extends FineAntsException {

	public ConflictException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ConflictException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
