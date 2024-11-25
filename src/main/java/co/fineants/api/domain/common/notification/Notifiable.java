package co.fineants.api.domain.common.notification;

import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;

public interface Notifiable {
	Long fetchMemberId();

	NotificationPreference getNotificationPreference();

	NotifyMessage createTargetGainMessageWith(String token);

	NotifyMessage createMaxLossMessageWith(String token);

	NotifyMessage createTargetPriceMessage(String token);
}
