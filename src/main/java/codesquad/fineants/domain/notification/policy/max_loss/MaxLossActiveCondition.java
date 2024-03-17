package codesquad.fineants.domain.notification.policy.max_loss;

import codesquad.fineants.domain.notification.policy.NotificationCondition;
import codesquad.fineants.domain.portfolio.Portfolio;

public class MaxLossActiveCondition implements NotificationCondition<Portfolio> {
	@Override
	public boolean isSatisfiedBy(Portfolio portfolio) {
		return portfolio.isSameMaxLossActive(true);
	}
}
