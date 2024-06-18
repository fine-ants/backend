package codesquad.fineants.domain.notification.domain.entity.policy.target_gain;

import java.util.Optional;

import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.policy.ConditionEvaluator;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationPolicy;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetGainNotificationPolicy implements NotificationPolicy<Portfolio> {

	private final ConditionEvaluator<Portfolio> portfolioConditionEvaluator;
	private final ConditionEvaluator<NotificationPreference> notificationPreferenceConditionEvaluator;

	@Override
	public Optional<NotifyMessage> apply(Portfolio portfolio, NotificationPreference preference,
		String token) {
		boolean result = portfolioConditionEvaluator.areConditionsSatisfied(portfolio)
			&& notificationPreferenceConditionEvaluator.areConditionsSatisfied(preference);
		if (result) {
			return Optional.of(portfolio.getTargetGainMessage(token));
		}
		return Optional.empty();
	}
}
