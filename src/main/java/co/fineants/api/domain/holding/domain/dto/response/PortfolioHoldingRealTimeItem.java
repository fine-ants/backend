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

	public static PortfolioHoldingRealTimeItem of(PortfolioHolding holding, Expression closingPrice,
		PortfolioCalculator calculator) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		Expression totalCurrentValuation = calculator.calTotalCurrentValuationBy(holding);
		Expression currentPrice = calculator.fetchCurrentPrice(holding);
		Expression dailyChange = calculator.calDailyChange(holding, closingPrice);
		Expression dailyChangeRate = calculator.calDailyChangeRate(holding, closingPrice);
		Expression totalGain = calculator.calTotalGainBy(holding);
		Percentage totalReturnPercentage = calculator.calTotalGainPercentage(holding);
		return new PortfolioHoldingRealTimeItem(
			holding.getId(),
			totalCurrentValuation.reduce(bank, to),
			currentPrice.reduce(bank, to),
			dailyChange.reduce(bank, to),
			dailyChangeRate.toPercentage(bank, to),
			totalGain.reduce(bank, to),
			totalReturnPercentage,
			holding.getCreateAt());
	}
}
