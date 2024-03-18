package codesquad.fineants.domain.notification.policy.target_gain;

import codesquad.fineants.domain.notification.policy.NotificationCondition;
import codesquad.fineants.domain.notification_preference.NotificationPreference;

public class TargetGainAccountPreferenceCondition implements NotificationCondition<NotificationPreference> {
	@Override
	public boolean isSatisfiedBy(NotificationPreference notificationPreference) {
		return notificationPreference.isPossibleTargetGainNotification();
	}
}
