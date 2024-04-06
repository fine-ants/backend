package codesquad.fineants.spring.api.stock_target_price.response;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.spring.api.notification.response.TargetPriceNotificationResponse;
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
public class TargetPriceNotifyMessageItem {
	private Long notificationId;
	private Boolean isRead;
	private String title;
	private String content;
	private NotificationType type;
	private String referenceId;
	private Long memberId;
	private String link;
	private String messageId;
	private String stockName;
	private Money targetPrice;
	private Long targetPriceNotificationId;

	public static TargetPriceNotifyMessageItem from(TargetPriceNotificationResponse response, String messageId) {
		return TargetPriceNotifyMessageItem.builder()
			.notificationId(response.getNotificationId())
			.isRead(response.getIsRead())
			.title(response.getTitle())
			.content(response.getContent())
			.type(response.getType())
			.referenceId(response.getReferenceId())
			.memberId(response.getMemberId())
			.link(response.getLink())
			.messageId(messageId)
			.stockName(response.getStockName())
			.targetPrice(response.getTargetPrice())
			.targetPriceNotificationId(response.getTargetPriceNotificationId())
			.build();
	}
}
