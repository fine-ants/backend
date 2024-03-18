package codesquad.fineants.spring.api.notification.response;

import codesquad.fineants.domain.notification.type.NotificationType;
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

	public static PortfolioNotifyMessageItem from(PortfolioNotificationResponse response, String messageId) {
		return PortfolioNotifyMessageItem.builder()
			.notificationId(response.getNotificationId())
			.isRead(response.getIsRead())
			.title(response.getTitle())
			.content(response.getContent())
			.type(response.getType())
			.referenceId(response.getReferenceId())
			.memberId(response.getMemberId())
			.link(response.getLink())
			.messageId(messageId)
			.name(response.getName())
			.build();
	}
}
