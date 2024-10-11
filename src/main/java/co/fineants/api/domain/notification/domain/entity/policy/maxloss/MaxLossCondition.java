package co.fineants.api.domain.notification.domain.entity.policy.maxloss;

import co.fineants.api.domain.notification.domain.entity.policy.NotificationCondition;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MaxLossCondition implements NotificationCondition<Portfolio> {

	private final PortfolioCalculator calculator;

	@Override
	public boolean isSatisfiedBy(Portfolio portfolio) {
		boolean result = calculator.reachedMaximumLossBy(portfolio);
		log.debug("MaxLossCondition.isSatisfieldBy : {}", result);
		return result;
	}
}
