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
	public boolean isSatisfied(Notifiable target) {
		boolean isPortfolioValid = portfolioConditions.stream()
			.allMatch(condition -> condition.test((Portfolio)target));
		boolean isPreferenceValid = preferenceConditions.test(target.getNotificationPreference());
		return isPortfolioValid && isPreferenceValid;
	}

	@Override
	public Optional<NotifyMessage> apply(Notifiable target, String token) {
		// TODO: will delete
		boolean isPortfolioValid = portfolioConditions.stream()
			.allMatch(condition -> condition.test((Portfolio)target));
		boolean isPreferenceValid = preferenceConditions.test(target.getNotificationPreference());

		if (isPortfolioValid && isPreferenceValid) {
			return Optional.of(target.createMaxLossMessageWith(token));
		}
		return Optional.empty();
	}
}
