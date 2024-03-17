package codesquad.fineants.domain.notification.policy.target_gain;

import codesquad.fineants.domain.notification.policy.NotificationCondition;
import codesquad.fineants.domain.portfolio.Portfolio;
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
