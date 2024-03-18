package codesquad.fineants.domain.notification.policy.max_loss;

import codesquad.fineants.domain.notification.policy.NotificationCondition;
import codesquad.fineants.domain.notification_preference.NotificationPreference;

public class MaxLossAccountPreferenceCondition implements NotificationCondition<NotificationPreference> {
	@Override
	public boolean isSatisfiedBy(NotificationPreference notificationPreference) {
		return notificationPreference.isPossibleMaxLossNotification();
	}
}
