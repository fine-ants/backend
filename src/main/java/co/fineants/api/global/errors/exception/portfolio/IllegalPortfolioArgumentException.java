package co.fineants.api.global.errors.exception.portfolio;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public abstract class IllegalPortfolioArgumentException extends IllegalArgumentException {

	private final ErrorCode errorCode;

	IllegalPortfolioArgumentException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	@Override
	public String toString() {
		return String.format("(errorCode=%s)", errorCode);
	}
}
