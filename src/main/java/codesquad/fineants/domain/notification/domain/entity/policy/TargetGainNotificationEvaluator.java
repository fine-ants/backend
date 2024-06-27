package codesquad.fineants.domain.notification.domain.entity.policy;

import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetGainNotificationEvaluator implements NotificationEvaluator<Portfolio> {

	private final ConditionEvaluator<Portfolio> portfolioConditionEvaluator;
	private final ConditionEvaluator<NotificationPreference> notificationPreferenceConditionEvaluator;

	@Override
	public boolean isSatisfiedBy(Portfolio target) {
		return portfolioConditionEvaluator.areConditionsSatisfied(target)
			&& notificationPreferenceConditionEvaluator.areConditionsSatisfied(target.getNotificationPreference());
	}
}
