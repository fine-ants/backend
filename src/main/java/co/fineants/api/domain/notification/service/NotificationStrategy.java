package co.fineants.api.domain.notification.service;

import java.util.function.Consumer;
import java.util.function.Function;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.policy.NotificationPolicy;

public interface NotificationStrategy {
	NotificationPolicy<Notifiable> getPolicy();

	Consumer<Notification> getSendHistory();

	Function<Notification, NotifyMessageItem> getMapper();
}
