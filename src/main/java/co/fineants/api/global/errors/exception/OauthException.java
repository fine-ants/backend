package co.fineants.api.global.errors.exception;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class OauthException extends FineAntsException {

	public OauthException(ErrorCode errorCode) {
		super(errorCode);
	}

	public OauthException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
