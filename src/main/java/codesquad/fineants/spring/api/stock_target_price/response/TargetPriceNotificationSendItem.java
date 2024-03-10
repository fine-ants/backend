package codesquad.fineants.spring.api.stock_target_price.response;

import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.spring.api.notification.response.NotifyMessageItem;
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
	private String title;
	private NotificationType type;
	private String referenceId;
	private String messageId;

	public static TargetPriceNotificationSendItem from(NotifyMessageItem item) {
		return TargetPriceNotificationSendItem.builder()
			.title(item.getTitle())
			.type(item.getType())
			.referenceId(item.getReferenceId())
			.messageId(item.getMessageId())
			.build();
	}
}
