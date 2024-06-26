package codesquad.fineants.domain.common.notification;

import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;

public interface Notifiable {
	Long fetchMemberId();

	NotifyMessage createTargetGainMessageWith(String token);

	NotificationPreference getNotificationPreference();

	NotifyMessage createMaxLossMessageWith(String token);
}
