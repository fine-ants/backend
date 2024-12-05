package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.List;
import java.util.function.Predicate;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;

public class TargetGainNotificationPolicy extends AbstractNotificationPolicy {

	public TargetGainNotificationPolicy(
		List<Predicate<Notifiable>> conditions,
		Predicate<NotificationPreference> preference
	) {
		super(conditions, preference);
	}
}
