package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.List;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConditionEvaluator<T> {
	private final List<Predicate<T>> conditions;

	public boolean isSatisfied(T target) {
		return conditions.stream()
			.allMatch(condition -> condition.test(target));
	}
}
