package codesquad.fineants.spring.api.portfolio.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
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
			.expectedMonthlyDividend(portfolio.calculateCurrentMonthDividend())
			.numShares(portfolio.getNumberOfShares())
			.dateCreated(portfolio.getCreateAt())
			.build();
	}
}
