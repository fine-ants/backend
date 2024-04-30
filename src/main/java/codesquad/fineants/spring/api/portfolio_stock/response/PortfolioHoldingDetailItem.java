package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class PortfolioHoldingDetailItem {
	private Long portfolioHoldingId;
	private Money currentValuation;
	private Money currentPrice;
	private Money averageCostPerShare;
	private Count numShares;
	private Money dailyChange;
	private Percentage dailyChangeRate;
	private Money totalGain;
	private Percentage totalReturnRate;
	private Money annualDividend;
	private Percentage annualDividendYield;

	public static PortfolioHoldingDetailItem from(PortfolioHolding portfolioHolding, Money lastDayClosingPrice) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return PortfolioHoldingDetailItem.builder()
			.portfolioHoldingId(portfolioHolding.getId())
			.currentValuation(portfolioHolding.calculateCurrentValuation().reduce(bank, to))
			.currentPrice(portfolioHolding.getCurrentPrice())
			.averageCostPerShare(portfolioHolding.calculateAverageCostPerShare().reduce(bank, to))
			.numShares(portfolioHolding.calculateNumShares())
			.dailyChange(portfolioHolding.calculateDailyChange(lastDayClosingPrice).reduce(bank, to))
			.dailyChangeRate(portfolioHolding.calculateDailyChangeRate(lastDayClosingPrice)
				.toPercentage(bank, to))
			.totalGain(portfolioHolding.calculateTotalGain().reduce(bank, to))
			.totalReturnRate(portfolioHolding.calculateTotalReturnRate().toPercentage(Bank.getInstance(), to))
			.annualDividend(portfolioHolding.calculateAnnualExpectedDividend())
			.annualDividendYield(
				portfolioHolding.calculateAnnualExpectedDividendYield().toPercentage(Bank.getInstance(), to))
			.build();
	}
}
