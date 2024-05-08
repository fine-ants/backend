package codesquad.fineants.global.errors.exception;

import codesquad.fineants.global.errors.errorcode.ErrorCode;

public class ForBiddenException extends FineAntsException {

	public ForBiddenException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ForBiddenException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
