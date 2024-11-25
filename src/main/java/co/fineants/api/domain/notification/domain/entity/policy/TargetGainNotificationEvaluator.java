package co.fineants.api.domain.notification.domain.entity.policy;

import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetGainNotificationEvaluator implements NotificationEvaluator<Portfolio> {

	private final ConditionEvaluator<Portfolio> portfolioConditionEvaluator;
	private final ConditionEvaluator<NotificationPreference> notificationPreferenceConditionEvaluator;

	@Override
	public boolean isSatisfiedBy(Portfolio target) {
		return portfolioConditionEvaluator.isSatisfied(target)
			&& notificationPreferenceConditionEvaluator.isSatisfied(target.getNotificationPreference());
	}
}
