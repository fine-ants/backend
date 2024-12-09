package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.List;
import java.util.function.Predicate;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;

public abstract class AbstractNotificationPolicy implements NotificationPolicy<Notifiable> {

	private final List<Predicate<Notifiable>> conditions;
	private final Predicate<NotificationPreference> preference;

	AbstractNotificationPolicy(
		List<Predicate<Notifiable>> conditions,
		Predicate<NotificationPreference> preference
	) {
		this.conditions = conditions;
		this.preference = preference;
	}

	public static TargetGainNotificationPolicy targetGainNotificationPolicy(
		List<Predicate<Notifiable>> conditions,
		Predicate<NotificationPreference> preference
	) {
		return new TargetGainNotificationPolicy(conditions, preference);
	}

	public static MaxLossNotificationPolicy maxLossNotificationPolicy(
		List<Predicate<Notifiable>> conditions,
		Predicate<NotificationPreference> preference
	) {
		return new MaxLossNotificationPolicy(conditions, preference);
	}

	public static TargetPriceNotificationPolicy targetPriceNotificationPolicy(
		List<Predicate<Notifiable>> conditions,
		Predicate<NotificationPreference> preference
	) {
		return new TargetPriceNotificationPolicy(conditions, preference);
	}

	@Override
	public boolean isSatisfied(Notifiable target) {
		boolean isValid = conditions.stream().allMatch(p -> p.test(target));
		if (!isValid) {
			return false;
		}
		return preference.test(target.getNotificationPreference());
	}
}
