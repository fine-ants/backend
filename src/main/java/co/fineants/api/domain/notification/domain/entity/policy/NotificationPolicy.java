package co.fineants.api.domain.notification.domain.entity.policy;

public interface NotificationPolicy<T> {

	boolean isSatisfied(T target);
}
