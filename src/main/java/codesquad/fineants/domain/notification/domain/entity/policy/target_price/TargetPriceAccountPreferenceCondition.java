package codesquad.fineants.domain.notification.domain.entity.policy.target_price;

import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.notification_preference.domain.entity.NotificationPreference;

public class TargetPriceAccountPreferenceCondition implements NotificationCondition<NotificationPreference> {
	@Override
	public boolean isSatisfiedBy(NotificationPreference notificationPreference) {
		return notificationPreference.isPossibleTargetGainNotification();
	}
}
