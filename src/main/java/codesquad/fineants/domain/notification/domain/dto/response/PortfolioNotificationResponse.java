package codesquad.fineants.domain.notification.domain.dto.response;

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
public class PortfolioNotificationResponse {
	private Long notificationId;
	private Boolean isRead;
	private String title;
	private String content;
	private NotificationType type;
	private String referenceId;
	private String link;
	private Long memberId;
	private String name;

	public static PortfolioNotificationResponse from(Notification notification) {
		return PortfolioNotificationResponse.builder()
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
}
