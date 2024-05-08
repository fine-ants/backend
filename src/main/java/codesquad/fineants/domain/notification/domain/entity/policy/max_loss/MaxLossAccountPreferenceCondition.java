package codesquad.fineants.domain.notification.domain.entity.policy.max_loss;

import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.notification_preference.domain.entity.NotificationPreference;

public class MaxLossAccountPreferenceCondition implements NotificationCondition<NotificationPreference> {
	@Override
	public boolean isSatisfiedBy(NotificationPreference notificationPreference) {
		return notificationPreference.isPossibleMaxLossNotification();
	}
}
