package co.fineants.api.domain.stock_target_price.domain.dto.response;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.dto.response.TargetPriceNotificationSaveResponse;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
@EqualsAndHashCode
public class TargetPriceNotifyMessageItem implements NotifyMessageItem, Comparable<TargetPriceNotifyMessageItem> {
	private Long notificationId;
	private Boolean isRead;
	private String title;
	private String content;
	private NotificationType type;
	private String referenceId;
	private Long memberId;
	private String link;
	private List<String> messageIds;
	private String stockName;
	private Money targetPrice;
	private Long targetPriceNotificationId;

	public static TargetPriceNotifyMessageItem from(TargetPriceNotificationSaveResponse response,
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

	public static TargetPriceNotifyMessageItem create(
		Long notificationId,
		Boolean isRead,
		String title,
		String content,
		NotificationType type,
		String referenceId,
		Long memberId,
		String link,
		List<String> messageIds,
		String stockName,
		Money targetPrice,
		Long targetPriceNotificationId) {
		return TargetPriceNotifyMessageItem.builder()
			.notificationId(notificationId)
			.isRead(isRead)
			.title(title)
			.content(content)
			.type(type)
			.referenceId(referenceId)
			.memberId(memberId)
			.link(link)
			.messageIds(messageIds)
			.stockName(stockName)
			.targetPrice(targetPrice)
			.targetPriceNotificationId(targetPriceNotificationId)
			.build();
	}

	@Override
	public int compareTo(@NotNull TargetPriceNotifyMessageItem item) {
		return Long.compare(notificationId, item.notificationId);
	}
}
