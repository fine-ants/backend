package codesquad.fineants.spring.api.stock_target_price.response;

import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.spring.api.notification.response.TargetPriceNotificationCreateResponse;
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
	private Long targetPriceNotificationId;
	private String title;
	private NotificationType type;
	private String referenceId;
	private String messageId;

	public static TargetPriceNotificationSendItem from(TargetPriceNotificationCreateResponse response) {
		return TargetPriceNotificationSendItem.builder()
			.notificationId(response.getNotificationId())
			.targetPriceNotificationId(response.getTargetPriceNotificationId())
			.title(response.getTitle())
			.type(response.getType())
			.referenceId(response.getReferenceId())
			.messageId(response.getMessageId())
			.build();
	}
}
