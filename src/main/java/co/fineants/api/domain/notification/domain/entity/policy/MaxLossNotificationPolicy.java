package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MaxLossNotificationPolicy implements NotificationPolicy<Notifiable> {

	private final List<Predicate<Portfolio>> portfolioConditions;
	private final Predicate<NotificationPreference> preferenceConditions;

	@Override
	public Optional<NotifyMessage> apply(Notifiable notifiable, String token) {
		boolean isPortfolioValid = portfolioConditions.stream()
			.allMatch(condition -> condition.test((Portfolio)notifiable));
		boolean isPreferenceValid = preferenceConditions.test(notifiable.getNotificationPreference());

		if (isPortfolioValid && isPreferenceValid) {
			return Optional.of(notifiable.createMaxLossMessageWith(token));
		}
		return Optional.empty();
	}
}
