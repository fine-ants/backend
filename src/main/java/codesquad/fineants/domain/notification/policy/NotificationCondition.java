package codesquad.fineants.domain.notification.policy;

public interface NotificationCondition<T> {
	boolean isSatisfiedBy(T t);
}
