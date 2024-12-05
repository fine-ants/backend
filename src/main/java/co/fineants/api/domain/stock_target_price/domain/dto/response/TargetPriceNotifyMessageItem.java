package co.fineants.api.domain.stock_target_price.domain.dto.response;

import java.util.List;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.dto.response.TargetPriceNotificationSaveResponse;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.StockTargetPriceNotification;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@ToString
@EqualsAndHashCode(callSuper = true)
public class TargetPriceNotifyMessageItem extends NotifyMessageItem {
	private String stockName;
	private Money targetPrice;
	private Long targetPriceNotificationId;

	public static NotifyMessageItem from(TargetPriceNotificationSaveResponse response,
		List<String> messageIds) {
		return TargetPriceNotifyMessageItem.builder()
			.notificationId(response.getNotificationId())
			.isRead(response.getIsRead())
			.title(response.getTitle())
			.content(response.getContent())
			.type(response.getType())
			.referenceId(response.getReferenceId())
			.memberId(response.getMemberId())
			.link(response.getLink())
			.messageIds(messageIds)
			.stockName(response.getStockName())
			.targetPrice(response.getTargetPrice())
			.targetPriceNotificationId(response.getTargetPriceNotificationId())
			.build();
	}

	public static NotifyMessageItem from(Notification notification) {
		StockTargetPriceNotification stock = (StockTargetPriceNotification)notification;
		return NotifyMessageItem.targetPriceNotifyMessageItem(
			notification.getId(),
			notification.getIsRead(),
			notification.getTitle(),
			notification.getContent(),
			notification.getType(),
			notification.getReferenceId(),
			notification.getMember().getId(),
			notification.getLink(),
			notification.getMessageIds(),
			stock.getStockName(),
			stock.getTargetPrice(),
			stock.getTargetPriceNotificationId()
		);
	}
}
