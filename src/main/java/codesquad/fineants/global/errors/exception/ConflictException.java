package codesquad.fineants.global.errors.exception;

import codesquad.fineants.global.errors.errorcode.ErrorCode;

public class ConflictException extends FineAntsException {

	public ConflictException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ConflictException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
