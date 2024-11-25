package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.List;
import java.util.function.Predicate;

import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetGainNotificationEvaluator implements NotificationEvaluator<Portfolio> {

	private final List<Predicate<Portfolio>> portfolioConditions;
	private final Predicate<NotificationPreference> notificationPreferenceConditionEvaluator;

	@Override
	public boolean isSatisfiedBy(Portfolio target) {
		return portfolioConditions.stream().allMatch(p -> p.test(target))
			&& notificationPreferenceConditionEvaluator.test(target.getNotificationPreference());
	}
}
