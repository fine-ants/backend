package co.fineants.api.global.errors.exception;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ApiRequestException extends FineAntsException {

	private final String responseBody;

	public ApiRequestException(ErrorCode errorCode, String responseBody) {
		super(errorCode);
		this.responseBody = responseBody;
	}

	@Override
	public String toString() {
		return String.format("ApiRequestException(responseBody=%s)", responseBody);
	}
}
