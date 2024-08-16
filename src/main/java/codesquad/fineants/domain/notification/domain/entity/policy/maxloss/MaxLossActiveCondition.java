package codesquad.fineants.domain.notification.domain.entity.policy.maxloss;

import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MaxLossActiveCondition implements NotificationCondition<Portfolio> {
	@Override
	public boolean isSatisfiedBy(Portfolio portfolio) {
		boolean result = portfolio.isSameMaxLossActive(true);
		log.debug("MaxLossActiveCondition.isSatisfiedBy : {}", result);
		return result;
	}
}
