package co.fineants.api.domain.stock_target_price.domain.dto.response;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.dto.response.TargetPriceNotificationSaveResponse;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
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
public class TargetPriceNotifyMessageItem implements NotifyMessageItem {
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

	public static TargetPriceNotifyMessageItem from(TargetPriceNotificationSaveResponse response, String messageId) {
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
