package codesquad.fineants.domain.notification.domain.entity.policy.maxloss;

import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;

public class MaxLossActiveCondition implements NotificationCondition<Portfolio> {
	@Override
	public boolean isSatisfiedBy(Portfolio portfolio) {
		return portfolio.isSameMaxLossActive(true);
	}
}
