package co.fineants.api.domain.notification.domain.dto.response;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotifyMessageItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
public abstract class NotifyMessageItem implements Comparable<NotifyMessageItem> {
	private Long notificationId;
	private Boolean isRead;
	private String title;
	private String content;
	private NotificationType type;
	private String referenceId;
	private Long memberId;
	private String link;
	private List<String> messageIds;

	public static NotifyMessageItem portfolioNotifyMessageItem(
		Long notificationId,
		Boolean isRead,
		String title,
		String content,
		NotificationType type,
		String referenceId,
		Long memberId,
		String link,
		String name,
		List<String> messageIds
	) {
		return PortfolioNotifyMessageItem.builder()
			.notificationId(notificationId)
			.isRead(isRead)
			.title(title)
			.content(content)
			.type(type)
			.referenceId(referenceId)
			.memberId(memberId)
			.link(link)
			.name(name)
			.messageIds(messageIds)
			.build();
	}

	public static NotifyMessageItem targetPriceNotifyMessageItem(
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
	public int compareTo(@NotNull NotifyMessageItem item) {
		return Long.compare(this.notificationId, item.notificationId);
	}
}
