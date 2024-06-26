package codesquad.fineants.domain.notification.domain.dto.response;

import codesquad.fineants.domain.notification.domain.dto.response.save.NotificationSaveResponse;
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
public class PortfolioNotifyMessageItem {
	private Long notificationId;
	private Boolean isRead;
	private String title;
	private String content;
	private NotificationType type;
	private String referenceId;
	private Long memberId;
	private String link;
	private String messageId;
	private String name;

	public static PortfolioNotifyMessageItem create(Long notificationId, Boolean isRead, String title,
		String content, NotificationType type, String referenceId, Long memberId, String link, String messageId,
		String name) {
		return PortfolioNotifyMessageItem.builder()
			.notificationId(notificationId)
			.isRead(isRead)
			.title(title)
			.content(content)
			.type(type)
			.referenceId(referenceId)
			.memberId(memberId)
			.link(link)
			.messageId(messageId)
			.name(name)
			.build();
	}

	public static PortfolioNotifyMessageItem from(NotificationSaveResponse response, String messageId) {
		return response.toNotifyMessageItemWith(messageId);
	}
}
