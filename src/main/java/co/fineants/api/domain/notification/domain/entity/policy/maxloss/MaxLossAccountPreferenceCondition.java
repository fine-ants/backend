package co.fineants.api.domain.notification.domain.entity.policy.maxloss;

import co.fineants.api.domain.notification.domain.entity.policy.NotificationCondition;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;

public class MaxLossAccountPreferenceCondition implements NotificationCondition<NotificationPreference> {
	@Override
	public boolean isSatisfiedBy(NotificationPreference notificationPreference) {
		return notificationPreference.isPossibleMaxLossNotification();
	}
}
