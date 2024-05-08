package codesquad.fineants.global.errors.exception;

import codesquad.fineants.global.errors.errorcode.ErrorCode;

public class ServerInternalException extends FineAntsException {

	public ServerInternalException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ServerInternalException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
