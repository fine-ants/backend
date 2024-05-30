package codesquad.fineants.domain.notification.domain.entity.policy.target_gain;

import java.util.List;
import java.util.Optional;

import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationPolicy;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetGainNotificationPolicy implements NotificationPolicy<Portfolio> {

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
			return Optional.of(portfolio.getTargetGainMessage(token));
		}
		return Optional.empty();
	}
}
