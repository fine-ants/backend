package co.fineants.api.global.errors.exception;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ServerInternalException extends FineAntsException {

	public ServerInternalException(ErrorCode errorCode, Throwable throwable) {
		super(errorCode, throwable);
	}
}
