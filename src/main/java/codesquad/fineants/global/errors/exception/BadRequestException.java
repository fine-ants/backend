package codesquad.fineants.global.errors.exception;

import codesquad.fineants.global.errors.errorcode.ErrorCode;

public class BadRequestException extends FineAntsException {

	public BadRequestException(ErrorCode errorCode) {
		super(errorCode);
	}

	public BadRequestException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
