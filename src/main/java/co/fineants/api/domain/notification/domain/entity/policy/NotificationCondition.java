package co.fineants.api.domain.notification.domain.entity.policy;

public interface NotificationCondition<T> {
	boolean isSatisfiedBy(T target);
}
