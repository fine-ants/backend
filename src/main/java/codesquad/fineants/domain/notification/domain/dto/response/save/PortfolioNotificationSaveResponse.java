package codesquad.fineants.domain.notification.domain.dto.response.save;

import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessageItem;
import codesquad.fineants.domain.notification.domain.dto.response.PortfolioNotifyMessageItem;
import codesquad.fineants.domain.notification.domain.entity.Notification;
import codesquad.fineants.domain.notification.domain.entity.type.NotificationType;
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
	public NotifyMessageItem toNotifyMessageItemWith(String messageId) {
		return PortfolioNotifyMessageItem.create(
			notificationId,
			isRead,
			title,
			content,
			type,
			referenceId,
			memberId,
			link,
			messageId,
			name
		);
	}
}
