package codesquad.fineants.spring.api.notification.response;

import codesquad.fineants.domain.notification.StockTargetPriceNotification;
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
public class TargetPriceNotificationCreateResponse {
	private Long notificationId;
	private Long targetPriceNotificationId;
	private String title;
	private Boolean isRead;
	private NotificationType type;
	private String referenceId;
	private String messageId;

	public static TargetPriceNotificationCreateResponse from(StockTargetPriceNotification notification) {
		return TargetPriceNotificationCreateResponse.builder()
			.notificationId(notification.getId())
			.targetPriceNotificationId(notification.getTargetPriceNotificationId())
			.title(notification.getTitle())
			.isRead(notification.getIsRead())
			.type(notification.getType())
			.referenceId(notification.getReferenceId())
			.messageId(notification.getMessageId())
			.build();
	}
}
