package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.List;
import java.util.function.Predicate;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MaxLossNotificationPolicy implements NotificationPolicy<Notifiable> {

	private final List<Predicate<Notifiable>> portfolioConditions;
	private final Predicate<NotificationPreference> preferenceConditions;

	@Override
	public boolean isSatisfied(Notifiable target) {
		boolean isPortfolioValid = portfolioConditions.stream()
			.allMatch(condition -> condition.test(target));
		boolean isPreferenceValid = preferenceConditions.test(target.getNotificationPreference());
		return isPortfolioValid && isPreferenceValid;
	}
}
