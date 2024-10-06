package co.fineants.api.global.errors.exception.portfolio;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.global.errors.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class IllegalPortfolioFinancialArgumentException extends IllegalPortfolioArgumentException {

	private final Money budget;
	private final Money targetGain;
	private final Money maximumLoss;

	public IllegalPortfolioFinancialArgumentException(Money budget, Money targetGain, Money maximumLoss,
		ErrorCode errorCode) {
		super(String.format("invalid PortfolioFinancial budget: %s, targetGain: %s, maximumLoss: %s",
			budget, targetGain, maximumLoss), errorCode);
		this.budget = budget;
		this.targetGain = targetGain;
		this.maximumLoss = maximumLoss;
	}

	@Override
	public String toString() {
		return String.format(
			"IllegalPortfolioFinancialArgumentException(budget=%s, targetGain=%s, maximumLoss=%s, errorCode=%s)",
			budget, targetGain, maximumLoss, getErrorCode());
	}
}
