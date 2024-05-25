package codesquad.fineants.domain.notification.domain.entity.policy.maxloss;

import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;

public class MaxLossAccountPreferenceCondition implements NotificationCondition<NotificationPreference> {
	@Override
	public boolean isSatisfiedBy(NotificationPreference notificationPreference) {
		return notificationPreference.isPossibleMaxLossNotification();
	}
}
