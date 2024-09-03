package co.fineants.api.domain.notification.domain.entity.policy;

public interface NotificationEvaluator<T> {
	boolean isSatisfiedBy(T target);
}
