package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
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

	public static PortfolioHoldingRealTimeItem of(PortfolioHolding portfolioHolding, Money lastDayClosingPrice) {
		Bank bank = Bank.getInstance();
		return new PortfolioHoldingRealTimeItem(
			portfolioHolding.calculateCurrentValuation().reduce(bank, Currency.KRW),
			portfolioHolding.getCurrentPrice(),
			portfolioHolding.calculateDailyChange(lastDayClosingPrice).reduce(bank, Currency.KRW),
			portfolioHolding.calculateDailyChangeRate(lastDayClosingPrice).toPercentage(bank, Currency.KRW),
			portfolioHolding.calculateTotalGain().reduce(bank, Currency.KRW),
			portfolioHolding.calculateTotalReturnRate().toPercentage(bank, Currency.KRW));
	}
}
