package codesquad.fineants.spring.api.errors.exception;

import codesquad.fineants.spring.api.errors.errorcode.ErrorCode;

public class NotFoundResourceException extends FineAntsException {

	public NotFoundResourceException(ErrorCode errorCode) {
		super(errorCode);
	}

	public NotFoundResourceException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
