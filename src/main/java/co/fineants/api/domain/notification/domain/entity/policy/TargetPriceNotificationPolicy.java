package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetPriceNotificationPolicy implements NotificationPolicy<Notifiable> {

	private final List<Predicate<TargetPriceNotification>> targetPriceConditions;
	private final Predicate<NotificationPreference> preferenceConditions;

	@Override
	public Optional<NotifyMessage> apply(Notifiable target, String token) {
		boolean isTargetPriceValid = targetPriceConditions.stream()
			.allMatch(condition -> condition.test((TargetPriceNotification)target));
		boolean isPreferenceValid = preferenceConditions.test(target.getNotificationPreference());
		if (isTargetPriceValid && isPreferenceValid) {
			return Optional.of(target.createTargetPriceMessage(token));
		}
		return Optional.empty();
	}
}
