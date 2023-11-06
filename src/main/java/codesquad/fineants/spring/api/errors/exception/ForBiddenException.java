package codesquad.fineants.spring.api.errors.exception;

import codesquad.fineants.spring.api.errors.errorcode.ErrorCode;

public class ForBiddenException extends FineAntsException {

	public ForBiddenException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ForBiddenException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
