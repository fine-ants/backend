package codesquad.fineants.domain.portfolio_holding.domain.dto.response;

import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.portfolio_holding.domain.entity.PortfolioHolding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioHoldingRealTimeItem {
	private Money currentValuation;
	private Money currentPrice;
	private Money dailyChange;
	private Percentage dailyChangeRate;
	private Money totalGain;
	private Percentage totalReturnRate;

	public static PortfolioHoldingRealTimeItem of(PortfolioHolding portfolioHolding, Expression lastDayClosingPrice) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return new PortfolioHoldingRealTimeItem(
			portfolioHolding.calculateCurrentValuation().reduce(bank, to),
			portfolioHolding.getCurrentPrice(),
			portfolioHolding.calculateDailyChange(lastDayClosingPrice).reduce(bank, to),
			portfolioHolding.calculateDailyChangeRate(lastDayClosingPrice).toPercentage(bank, to),
			portfolioHolding.calculateTotalGain().reduce(bank, to),
			portfolioHolding.calculateTotalReturnRate().toPercentage(bank, to));
	}
}
