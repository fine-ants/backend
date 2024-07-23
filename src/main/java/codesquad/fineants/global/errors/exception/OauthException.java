package codesquad.fineants.global.errors.exception;

import codesquad.fineants.global.errors.errorcode.ErrorCode;

public class OauthException extends FineAntsException {

	public OauthException(ErrorCode errorCode) {
		super(errorCode);
	}

	public OauthException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
