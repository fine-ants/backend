package co.fineants.api.global.errors.exception.portfolio;

import co.fineants.api.domain.portfolio.domain.entity.PortfolioFinancial;
import co.fineants.api.global.errors.errorcode.ErrorCode;

public class IllegalPortfolioFinancialStateException extends IllegalPortfolioStateException {
	private final PortfolioFinancial financial;

	public IllegalPortfolioFinancialStateException(PortfolioFinancial financial, ErrorCode errorCode) {
		super(String.format("illegal financial state, financial=%s", financial), errorCode);
		this.financial = financial;
	}

	@Override
	public String toString() {
		return String.format("IllegalPortfolioFinancialStateException(financial=%s, %s)", financial, super.toString());
	}
}
