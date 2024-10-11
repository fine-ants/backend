package co.fineants.api.domain.notification.domain.entity.policy.target_gain;

import co.fineants.api.domain.notification.domain.entity.policy.NotificationCondition;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TargetGainCondition implements NotificationCondition<Portfolio> {

	private final PortfolioCalculator calculator;

	@Override
	public boolean isSatisfiedBy(Portfolio portfolio) {
		boolean result = calculator.reachedTargetGainBy(portfolio);
		log.debug("TargetGainCondition isSatisfiedBy: {}", result);
		return result;
	}
}
