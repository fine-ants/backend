package codesquad.fineants.spring.api.member.response;

import java.time.LocalDateTime;
import java.util.List;

import codesquad.fineants.domain.notification.Notification;
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
public class MemberNotificationSendResponse {
	private Long notificationId;
	private String title;
	private String content;
	private LocalDateTime timestamp;
	private Boolean isRead;
	private String type;
	private String referenceId;
	private List<String> sendMessageIds;

	public static MemberNotificationSendResponse from(Notification notification, List<String> sendMessageIds) {
		return MemberNotificationSendResponse.builder()
			.notificationId(notification.getId())
			.title(notification.getTitle())
			.content(notification.getContent())
			.timestamp(notification.getCreateAt())
			.isRead(notification.getIsRead())
			.type(notification.getType().getCategory())
			.referenceId(notification.getReferenceId())
			.sendMessageIds(sendMessageIds)
			.build();
	}
}
