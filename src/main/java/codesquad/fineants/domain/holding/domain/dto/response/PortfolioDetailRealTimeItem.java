package codesquad.fineants.domain.holding.domain.dto.response;

import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.gainhistory.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioDetailRealTimeItem {
	private Money currentValuation;
	private Money totalGain;
	private Percentage totalGainRate;
	private Money dailyGain;
	private Percentage dailyGainRate;
	private Money provisionalLossBalance;

	public static PortfolioDetailRealTimeItem of(Portfolio portfolio, PortfolioGainHistory history) {
		Bank bank = Bank.getInstance();
		return new PortfolioDetailRealTimeItem(
			portfolio.calculateTotalCurrentValuation().reduce(bank, Currency.KRW),
			portfolio.calculateTotalGain().reduce(bank, Currency.KRW),
			portfolio.calculateTotalGainRate().toPercentage(Bank.getInstance(), Currency.KRW),
			portfolio.calculateDailyGain(history).reduce(bank, Currency.KRW),
			portfolio.calculateDailyGainRate(history).toPercentage(Bank.getInstance(), Currency.KRW),
			Money.zero()
		);
	}
}
