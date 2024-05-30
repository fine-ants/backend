package codesquad.fineants.domain.notification.domain.entity.policy;

import java.util.Optional;

import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;

public interface NotificationPolicy<T> {
	Optional<NotifyMessage> apply(T target, NotificationPreference preference, String token);
}
