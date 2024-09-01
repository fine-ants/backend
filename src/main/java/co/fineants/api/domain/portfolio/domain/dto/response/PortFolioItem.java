package co.fineants.api.domain.portfolio.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
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
public class PortFolioItem {
	private Long id;
	private String securitiesFirm;
	private String name;
	private Money budget;
	private Money totalGain;
	private Percentage totalGainRate;
	private Money dailyGain;
	private Percentage dailyGainRate;
	private Money currentValuation;
	private Money expectedMonthlyDividend;
	private Count numShares;
	private LocalDateTime dateCreated;

	public static PortFolioItem of(Portfolio portfolio, PortfolioGainHistory prevHistory) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return PortFolioItem.builder()
			.id(portfolio.getId())
			.securitiesFirm(portfolio.getSecuritiesFirm())
			.name(portfolio.getName())
			.budget(portfolio.getBudget())
			.totalGain(portfolio.calculateTotalGain().reduce(bank, to))
			.totalGainRate(portfolio.calculateTotalGainRate().toPercentage(Bank.getInstance(), Currency.KRW))
			.dailyGain(portfolio.calculateDailyGain(prevHistory).reduce(bank, to))
			.dailyGainRate(portfolio.calculateDailyGainRate(prevHistory).toPercentage(Bank.getInstance(), Currency.KRW))
			.currentValuation(portfolio.calculateTotalCurrentValuation().reduce(bank, to))
			.expectedMonthlyDividend(portfolio.calculateCurrentMonthDividend().reduce(bank, to))
			.numShares(portfolio.getNumberOfShares())
			.dateCreated(portfolio.getCreateAt())
			.build();
	}
}
