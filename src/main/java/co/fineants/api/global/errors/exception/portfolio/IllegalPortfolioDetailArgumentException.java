package co.fineants.api.global.errors.exception.portfolio;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class IllegalPortfolioDetailArgumentException extends IllegalArgumentException {
	private final ErrorCode errorCode;

	public IllegalPortfolioDetailArgumentException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
