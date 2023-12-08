package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioDetailRealTimeItem {
	private Long currentValuation;
	private Long totalGain;
	private Integer totalGainRate;
	private Long dailyGain;
	private Integer dailyGainRate;
	private Long provisionalLossBalance;

	public static PortfolioDetailRealTimeItem of(Portfolio portfolio, PortfolioGainHistory history) {
		return new PortfolioDetailRealTimeItem(
			portfolio.calculateTotalCurrentValuation(),
			portfolio.calculateTotalGain(),
			portfolio.calculateTotalGainRate(),
			portfolio.calculateDailyGain(history),
			portfolio.calculateDailyGainRate(history),
			0L
		);
	}
}
