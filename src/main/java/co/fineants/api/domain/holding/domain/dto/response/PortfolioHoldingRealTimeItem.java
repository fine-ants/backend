package co.fineants.api.domain.holding.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
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
	private Long id;
	private Money currentValuation;
	private Money currentPrice;
	private Money dailyChange;
	private Percentage dailyChangeRate;
	private Money totalGain;
	private Percentage totalReturnRate;
	private LocalDateTime dateAdded;

	public static PortfolioHoldingRealTimeItem of(PortfolioHolding portfolioHolding, Expression lastDayClosingPrice,
		PortfolioCalculator calculator) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		Expression totalCurrentValuation = calculator.calTotalCurrentValuation(portfolioHolding);
		Expression dailyChange = calculator.calDailyChange(portfolioHolding, lastDayClosingPrice);
		Expression dailyChangeRate = calculator.calDailyChangeRate(portfolioHolding, lastDayClosingPrice);
		Expression totalGain = calculator.calTotalGainBy(portfolioHolding);
		Percentage totalReturnPercentage = calculator.calTotalReturnPercentage(portfolioHolding);
		return new PortfolioHoldingRealTimeItem(
			portfolioHolding.getId(),
			totalCurrentValuation.reduce(bank, to),
			portfolioHolding.getCurrentPrice(),
			dailyChange.reduce(bank, to),
			dailyChangeRate.toPercentage(bank, to),
			totalGain.reduce(bank, to),
			totalReturnPercentage,
			portfolioHolding.getCreateAt());
	}
}
