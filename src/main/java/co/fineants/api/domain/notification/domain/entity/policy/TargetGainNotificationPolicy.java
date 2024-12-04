package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetGainNotificationPolicy implements NotificationPolicy<Notifiable> {

	private final List<Predicate<Notifiable>> portfolioConditions;
	private final Predicate<NotificationPreference> preferencePredicate;

	@Override
	public boolean isSatisfied(Notifiable target) {
		boolean isPortfolioValid = portfolioConditions.stream().allMatch(p -> p.test(target));
		boolean isPreferenceValid = preferencePredicate.test(target.getNotificationPreference());
		return isPortfolioValid && isPreferenceValid;
	}

	@Override
	public Optional<NotifyMessage> apply(Notifiable target, String token) {
		return Optional.of(target.createMessage(token));
	}
}
