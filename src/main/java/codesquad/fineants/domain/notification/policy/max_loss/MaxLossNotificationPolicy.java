package codesquad.fineants.domain.notification.policy.max_loss;

import java.util.List;
import java.util.Optional;

import codesquad.fineants.domain.notification.policy.NotificationCondition;
import codesquad.fineants.domain.notification.policy.NotificationPolicy;
import codesquad.fineants.domain.notification_preference.NotificationPreference;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.spring.api.notification.response.NotifyMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MaxLossNotificationPolicy implements NotificationPolicy<Portfolio> {

	private final List<NotificationCondition<Portfolio>> portfolioConditions;
	private final List<NotificationCondition<NotificationPreference>> preferenceConditions;

	@Override
	public Optional<NotifyMessage> apply(Portfolio portfolio, NotificationPreference preference,
		String token) {
		boolean result = portfolioConditions.stream()
			.allMatch(condition -> condition.isSatisfiedBy(portfolio))
			&& preferenceConditions.stream()
			.allMatch(condition -> condition.isSatisfiedBy(preference));
		if (result) {
			return Optional.of(portfolio.getMaxLossMessage(token));
		}
		return Optional.empty();
	}
}
