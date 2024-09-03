package co.fineants.api.domain.notification.domain.entity.policy.target_gain;

import co.fineants.api.domain.notification.domain.entity.policy.NotificationCondition;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetGainCondition implements NotificationCondition<Portfolio> {

	@Override
	public boolean isSatisfiedBy(Portfolio portfolio) {
		boolean result = portfolio.reachedTargetGain();
		log.debug("TargetGainCondition isSatisfiedBy: {}", result);
		return result;
	}
}
