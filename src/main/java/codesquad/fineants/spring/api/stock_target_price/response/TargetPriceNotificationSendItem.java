package codesquad.fineants.spring.api.stock_target_price.response;

import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.spring.api.notification.response.NotificationCreateResponse;
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
public class TargetPriceNotificationSendItem {
	private Long notificationId;
	private String title;
	private NotificationType type;
	private String referenceId;
	private String messageId;

	public static TargetPriceNotificationSendItem from(NotificationCreateResponse response, String messageId) {
		return TargetPriceNotificationSendItem.builder()
			.notificationId(response.getNotificationId())
			.title(response.getTitle())
			.type(response.getType())
			.referenceId(response.getReferenceId())
			.messageId(messageId)
			.build();
	}
}
