package codesquad.fineants.spring.api.common.errors.exception;

import codesquad.fineants.spring.api.common.errors.errorcode.ErrorCode;

public class ServerInternalException extends FineAntsException {

	public ServerInternalException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ServerInternalException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
