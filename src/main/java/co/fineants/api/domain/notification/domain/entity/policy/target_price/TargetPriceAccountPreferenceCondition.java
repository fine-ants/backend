package co.fineants.api.domain.notification.domain.entity.policy.target_price;

import co.fineants.api.domain.notification.domain.entity.policy.NotificationCondition;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;

public class TargetPriceAccountPreferenceCondition implements NotificationCondition<NotificationPreference> {
	@Override
	public boolean isSatisfiedBy(NotificationPreference notificationPreference) {
		return notificationPreference.isPossibleStockTargetPriceNotification();
	}
}
