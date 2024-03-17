package codesquad.fineants.domain.notification.policy;

import java.util.Optional;

import codesquad.fineants.domain.notification_preference.NotificationPreference;
import codesquad.fineants.spring.api.notification.response.NotifyMessage;

public interface NotificationPolicy<T> {
	Optional<NotifyMessage> apply(T t, NotificationPreference preference, String token);
}
