package co.fineants.api.domain.notification.domain.entity.policy.maxloss;

import co.fineants.api.domain.notification.domain.entity.policy.NotificationCondition;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;

public class MaxLossActiveCondition implements NotificationCondition<Portfolio> {
	@Override
	public boolean isSatisfiedBy(Portfolio portfolio) {
		return portfolio.isSameMaxLossActive(true);
	}
}
