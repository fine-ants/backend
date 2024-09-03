package co.fineants.api.domain.common.notification;

import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;

public interface Notifiable {
	Long fetchMemberId();

	NotifyMessage createTargetGainMessageWith(String token);

	NotificationPreference getNotificationPreference();

	NotifyMessage createMaxLossMessageWith(String token);

	NotifyMessage getTargetPriceMessage(String token);
}
