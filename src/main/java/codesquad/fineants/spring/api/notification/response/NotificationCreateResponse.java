package codesquad.fineants.spring.api.notification.response;

import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.type.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class NotificationCreateResponse {
	private Long notificationId;
	private String title;
	private Boolean isRead;
	private NotificationType type;
	private String referenceId;

	public static NotificationCreateResponse from(Notification notification) {
		return NotificationCreateResponse.builder()
			.notificationId(notification.getId())
			.title(notification.getTitle())
			.isRead(notification.getIsRead())
			.type(notification.getType())
			.referenceId(notification.getReferenceId())
			.build();
	}
}
