package codesquad.fineants.spring.api.common.errors.exception;

import codesquad.fineants.spring.api.common.errors.errorcode.ErrorCode;

public class OauthException extends FineAntsException {

	public OauthException(ErrorCode errorCode) {
		super(errorCode);
	}

	public OauthException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
