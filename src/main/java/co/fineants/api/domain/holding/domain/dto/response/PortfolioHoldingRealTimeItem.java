package co.fineants.api.domain.holding.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
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

	public static PortfolioHoldingRealTimeItem of(PortfolioHolding portfolioHolding, Expression lastDayClosingPrice) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return new PortfolioHoldingRealTimeItem(
			portfolioHolding.getId(),
			portfolioHolding.calculateCurrentValuation().reduce(bank, to),
			portfolioHolding.getCurrentPrice(),
			portfolioHolding.calculateDailyChange(lastDayClosingPrice).reduce(bank, to),
			portfolioHolding.calculateDailyChangeRate(lastDayClosingPrice).toPercentage(bank, to),
			portfolioHolding.calculateTotalGain().reduce(bank, to),
			portfolioHolding.calculateTotalReturnRate().toPercentage(bank, to),
			portfolioHolding.getCreateAt());
	}
}
