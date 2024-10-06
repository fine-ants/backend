package co.fineants.api.global.errors.exception.portfolio;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class IllegalPortfolioSecuritiesFirmArgumentException extends IllegalPortfolioArgumentException {
	private final String securitiesFirm;

	public IllegalPortfolioSecuritiesFirmArgumentException(String securitiesFirm, ErrorCode errorCode) {
		super(String.format("Unlisted securitiesFirm: %s", securitiesFirm), errorCode);
		this.securitiesFirm = securitiesFirm;
	}

	@Override
	public String toString() {
		return String.format("IllegalPortfolioSecuritiesFirmArgumentException(securitiesFirm=%s, %s)", securitiesFirm,
			super.toString());
	}
}
