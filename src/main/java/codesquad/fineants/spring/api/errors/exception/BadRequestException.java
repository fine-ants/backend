package codesquad.fineants.spring.api.errors.exception;

import codesquad.fineants.spring.api.errors.errorcode.ErrorCode;

public class BadRequestException extends FineAntsException {

	public BadRequestException(ErrorCode errorCode) {
		super(errorCode);
	}

	public BadRequestException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
