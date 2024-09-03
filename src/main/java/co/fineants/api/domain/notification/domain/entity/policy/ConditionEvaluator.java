package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConditionEvaluator<T> {
	private final List<NotificationCondition<T>> conditions;

	public boolean areConditionsSatisfied(T target) {
		return conditions.stream()
			.allMatch(condition -> condition.isSatisfiedBy(target));
	}
}
