package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
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
	private Long currentValuation;
	private Long totalGain;
	private Double totalGainRate;
	private Long dailyGain;
	private Double dailyGainRate;
	private Long provisionalLossBalance;

	public static PortfolioDetailRealTimeItem of(Portfolio portfolio, PortfolioGainHistory history) {
		return new PortfolioDetailRealTimeItem(
			portfolio.calculateTotalCurrentValuation().getAmount().longValue(),
			portfolio.calculateTotalGain().getAmount().longValue(),
			portfolio.calculateTotalGainRate(),
			portfolio.calculateDailyGain(history).getAmount().longValue(),
			portfolio.calculateDailyGainRate(history),
			0L
		);
	}
}
