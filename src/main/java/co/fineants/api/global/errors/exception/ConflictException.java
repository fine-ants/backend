package co.fineants.api.global.errors.exception;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ConflictException extends FineAntsException {

	public ConflictException(ErrorCode errorCode) {
		super(errorCode);
	}
}
