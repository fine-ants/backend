package co.fineants.api.global.errors.exception.portfolio;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class IllegalPortfolioNameArgumentException extends IllegalPortfolioArgumentException {
	private final String name;

	public IllegalPortfolioNameArgumentException(String name, ErrorCode errorCode) {
		super(String.format("Invalid Portfolio name: %s", name), errorCode);
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("IllegalPortfolioNameArgumentException(name=%s, %s)", name, super.toString());
	}
}
