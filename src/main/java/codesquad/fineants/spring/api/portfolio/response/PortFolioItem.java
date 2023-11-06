package codesquad.fineants.spring.api.portfolio.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PortFolioItem {
	private Long id;
	private String securitiesFirm;
	private String name;
	private Long budget;
	private Long totalGain;
	private Integer totalGainRate;
	private Long dailyGain;
	private Integer dailyGainRate;
	private Long expectedMonthlyDividend;
	private Integer numShares;

	@Builder(access = AccessLevel.PRIVATE)
	private PortFolioItem(Long id, String securitiesFirm, String name, Long budget, Long totalGain,
		Integer totalGainRate,
		Long dailyGain, Integer dailyGainRate, Long expectedMonthlyDividend, Integer numShares) {
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
			.expectedMonthlyDividend(portfolio.calculateExpectedMonthlyDividend(LocalDateTime.now()))
			.numShares(portfolio.getNumberOfShares())
			.build();
	}
}
