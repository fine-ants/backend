package co.fineants.api.global.errors.exception.portfolio;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class IllegalPortfolioStateException extends IllegalStateException {
	private final ErrorCode errorCode;

	IllegalPortfolioStateException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	@Override
	public String toString() {
		return String.format("(errorCode=%s)", errorCode);
	}
}
