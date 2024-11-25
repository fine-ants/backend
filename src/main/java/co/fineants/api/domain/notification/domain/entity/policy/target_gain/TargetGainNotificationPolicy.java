package co.fineants.api.domain.notification.domain.entity.policy.target_gain;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.entity.policy.NotificationPolicy;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetGainNotificationPolicy implements NotificationPolicy<Notifiable> {
	
	private final List<Predicate<Portfolio>> portfolioConditions;
	private final Predicate<NotificationPreference> preferencePredicate;

	@Override
	public Optional<NotifyMessage> apply(Notifiable notifiable, String token) {
		Portfolio portfolio = (Portfolio)notifiable;
		boolean isPortfolioValid = portfolioConditions.stream().allMatch(p -> p.test(portfolio));
		boolean isPreferenceValid = preferencePredicate.test(portfolio.getNotificationPreference());

		if (isPortfolioValid && isPreferenceValid) {
			return Optional.of(notifiable.createTargetGainMessageWith(token));
		}
		return Optional.empty();
	}
}
