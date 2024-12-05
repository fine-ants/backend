package co.fineants.api.domain.notification.domain.dto.response.save;

import java.util.List;

import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.dto.response.PortfolioNotifyMessageItem;
import co.fineants.api.domain.notification.domain.entity.Notification;
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
@Builder(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioNotificationSaveResponse implements NotificationSaveResponse {
	private Long notificationId;
	private Boolean isRead;
	private String title;
	private String content;
	private NotificationType type;
	private String referenceId;
	private String link;
	private Long memberId;
	private String name;

	public static PortfolioNotificationSaveResponse from(Notification notification) {
		return PortfolioNotificationSaveResponse.builder()
			.notificationId(notification.getId())
			.isRead(notification.getIsRead())
			.title(notification.getTitle())
			.content(notification.getContent())
			.type(notification.getType())
			.referenceId(notification.getReferenceId())
			.link(notification.getLink())
			.memberId(notification.getMember().getId())
			.name(notification.getName())
			.build();
	}

	@Override
	public String getIdToSentHistory() {
		return String.format("portfolioNotification:%d", notificationId);
	}

	@Override
	public NotifyMessageItem toNotifyMessageItemWith(List<String> messageIds) {
		return PortfolioNotifyMessageItem.create(
			notificationId,
			isRead,
			title,
			content,
			type,
			referenceId,
			memberId,
			link,
			name,
			messageIds
		);
	}
}
