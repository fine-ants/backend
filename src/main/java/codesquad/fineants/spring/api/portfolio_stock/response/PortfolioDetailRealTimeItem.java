package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.common.money.Money;
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
	private Money currentValuation;
	private Money totalGain;
	private Double totalGainRate;
	private Money dailyGain;
	private Double dailyGainRate;
	private Money provisionalLossBalance;

	public static PortfolioDetailRealTimeItem of(Portfolio portfolio, PortfolioGainHistory history) {
		return new PortfolioDetailRealTimeItem(
			portfolio.calculateTotalCurrentValuation(),
			portfolio.calculateTotalGain(),
			portfolio.calculateTotalGainRate(),
			portfolio.calculateDailyGain(history),
			portfolio.calculateDailyGainRate(history),
			Money.zero()
		);
	}
}
