package codesquad.fineants.domain.notification.domain.entity.policy.target_gain;

import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;

public class TargetGainAccountPreferenceCondition implements NotificationCondition<NotificationPreference> {
	@Override
	public boolean isSatisfiedBy(NotificationPreference notificationPreference) {
		return notificationPreference.isPossibleTargetGainNotification();
	}
}
