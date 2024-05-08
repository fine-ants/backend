package codesquad.fineants.domain.notification.domain.entity.policy;

public interface NotificationCondition<T> {
	boolean isSatisfiedBy(T t);
}
