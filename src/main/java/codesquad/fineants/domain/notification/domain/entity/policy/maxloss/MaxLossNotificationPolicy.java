package codesquad.fineants.domain.notification.domain.entity.policy.maxloss;

import java.util.List;
import java.util.Optional;

import codesquad.fineants.domain.common.notification.Notifiable;
import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationPolicy;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
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
