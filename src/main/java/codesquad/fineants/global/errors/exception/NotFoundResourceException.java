package codesquad.fineants.global.errors.exception;

import codesquad.fineants.global.errors.errorcode.ErrorCode;

public class NotFoundResourceException extends FineAntsException {

	public NotFoundResourceException(ErrorCode errorCode) {
		super(errorCode);
	}

	public NotFoundResourceException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
