package co.fineants.api.domain.notification.domain.entity.policy.maxloss;

import java.util.List;
import java.util.Optional;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.entity.policy.NotificationCondition;
import co.fineants.api.domain.notification.domain.entity.policy.NotificationPolicy;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MaxLossNotificationPolicy implements NotificationPolicy<Notifiable> {

	private final List<NotificationCondition<Portfolio>> portfolioConditions;
	private final List<NotificationCondition<NotificationPreference>> preferenceConditions;

	@Override
	public Optional<NotifyMessage> apply(Notifiable notifiable, String token) {
		boolean result = portfolioConditions.stream()
			.allMatch(condition -> condition.isSatisfiedBy((Portfolio)notifiable))
			&& preferenceConditions.stream()
			.allMatch(condition -> condition.isSatisfiedBy(notifiable.getNotificationPreference()));
		if (result) {
			return Optional.of(notifiable.createMaxLossMessageWith(token));
		}
		return Optional.empty();
	}
}
