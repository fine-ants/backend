package codesquad.fineants.domain.notification.policy.target_price;

import java.util.List;
import java.util.Optional;

import codesquad.fineants.domain.notification.policy.NotificationCondition;
import codesquad.fineants.domain.notification.policy.NotificationPolicy;
import codesquad.fineants.domain.notification_preference.NotificationPreference;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.spring.api.notification.response.NotifyMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetPriceNotificationPolicy implements NotificationPolicy<TargetPriceNotification> {

	private final List<NotificationCondition<TargetPriceNotification>> targetPriceConditions;
	private final List<NotificationCondition<NotificationPreference>> preferenceConditions;

	@Override
	public Optional<NotifyMessage> apply(TargetPriceNotification targetPriceNotification,
		NotificationPreference preference,
		String token) {
		boolean result = targetPriceConditions.stream()
			.allMatch(condition -> condition.isSatisfiedBy(targetPriceNotification))
			&& preferenceConditions.stream()
			.allMatch(condition -> condition.isSatisfiedBy(preference));
		if (result) {
			return Optional.of(targetPriceNotification.getTargetPriceMessage(token));
		}
		return Optional.empty();
	}
}
