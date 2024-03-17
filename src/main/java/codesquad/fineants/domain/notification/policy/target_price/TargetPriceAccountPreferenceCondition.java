package codesquad.fineants.domain.notification.policy.target_price;

import codesquad.fineants.domain.notification.policy.NotificationCondition;
import codesquad.fineants.domain.notification_preference.NotificationPreference;

public class TargetPriceAccountPreferenceCondition implements NotificationCondition<NotificationPreference> {
	@Override
	public boolean isSatisfiedBy(NotificationPreference notificationPreference) {
		return notificationPreference.isPossibleTargetGainNotification();
	}
}
