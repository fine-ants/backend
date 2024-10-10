package co.fineants.api.domain.holding.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
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
	private Long id;
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
	private LocalDateTime dateAdded;

	public static PortfolioHoldingDetailItem from(PortfolioHolding portfolioHolding, Expression lastDayClosingPrice,
		PortfolioCalculator calculator) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		Expression totalCurrentValuation = calculator.calTotalCurrentValuation(portfolioHolding);
		Expression annualDividendYield = calculator.calAnnualExpectedDividendYieldBy(portfolioHolding);
		Percentage totalReturnPercentage = calculator.calTotalReturnPercentage(portfolioHolding);
		return PortfolioHoldingDetailItem.builder()
			.id(portfolioHolding.getId())
			.currentValuation(totalCurrentValuation.reduce(bank, to))
			.currentPrice(portfolioHolding.getCurrentPrice())
			.averageCostPerShare(portfolioHolding.calculateAverageCostPerShare().reduce(bank, to))
			.numShares(portfolioHolding.calculateNumShares())
			.dailyChange(portfolioHolding.calculateDailyChange(lastDayClosingPrice).reduce(bank, to))
			.dailyChangeRate(portfolioHolding.calculateDailyChangeRate(lastDayClosingPrice)
				.toPercentage(bank, to))
			.totalGain(portfolioHolding.calculateTotalGain().reduce(bank, to))
			.totalReturnRate(totalReturnPercentage)
			.annualDividend(portfolioHolding.calculateAnnualExpectedDividend().reduce(bank, to))
			.annualDividendYield(annualDividendYield.toPercentage(Bank.getInstance(), to))
			.dateAdded(portfolioHolding.getCreateAt())
			.build();
	}
}
