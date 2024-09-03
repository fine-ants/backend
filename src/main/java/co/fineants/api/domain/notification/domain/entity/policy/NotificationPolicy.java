package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.Optional;

import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;

public interface NotificationPolicy<T> {
	Optional<NotifyMessage> apply(T target, String token);
}
