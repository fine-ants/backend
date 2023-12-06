package codesquad.fineants.spring.api.portfolio.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PortFolioItem {
	private final Long id;
	private final String securitiesFirm;
	private final String name;
	private final Long budget;
	private final Long totalGain;
	private final Integer totalGainRate;
	private final Long dailyGain;
	private final Integer dailyGainRate;
	private final Long expectedMonthlyDividend;
	private final Integer numShares;

	private final LocalDateTime dateCreated;

	@Builder(access = AccessLevel.PRIVATE)
	private PortFolioItem(Long id, String securitiesFirm, String name, Long budget, Long totalGain,
		Integer totalGainRate,
		Long dailyGain, Integer dailyGainRate, Long expectedMonthlyDividend, Integer numShares,
		LocalDateTime dateCreated) {
		this.id = id;
		this.securitiesFirm = securitiesFirm;
		this.name = name;
		this.budget = budget;
		this.totalGain = totalGain;
		this.totalGainRate = totalGainRate;
		this.dailyGain = dailyGain;
		this.dailyGainRate = dailyGainRate;
		this.expectedMonthlyDividend = expectedMonthlyDividend;
		this.numShares = numShares;
		this.dateCreated = dateCreated;
	}

	public static PortFolioItem of(Portfolio portfolio, PortfolioGainHistory prevHistory) {
		return PortFolioItem.builder()
			.id(portfolio.getId())
			.securitiesFirm(portfolio.getSecuritiesFirm())
			.name(portfolio.getName())
			.budget(portfolio.getBudget())
			.totalGain(portfolio.calculateTotalGain())
			.totalGainRate(portfolio.calculateTotalGainRate())
			.dailyGain(portfolio.calculateDailyGain(prevHistory))
			.dailyGainRate(portfolio.calculateDailyGainRate(prevHistory))
			.expectedMonthlyDividend(portfolio.calculateCurrentMonthDividend())
			.numShares(portfolio.getNumberOfShares())
			.dateCreated(portfolio.getCreateAt())
			.build();
	}
}
