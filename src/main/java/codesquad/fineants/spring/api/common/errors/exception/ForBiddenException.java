package codesquad.fineants.spring.api.common.errors.exception;

import codesquad.fineants.spring.api.common.errors.errorcode.ErrorCode;

public class ForBiddenException extends FineAntsException {

	public ForBiddenException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ForBiddenException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
