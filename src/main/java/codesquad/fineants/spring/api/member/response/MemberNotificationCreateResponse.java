package codesquad.fineants.spring.api.member.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.notification.Notification;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MemberNotificationCreateResponse {
	private Long notificationId;
	private String title;
	private String content;
	private LocalDateTime timestamp;
	private Boolean isRead;
	private String type;
	private String referenceId;

	public static MemberNotificationCreateResponse from(Notification notification) {
		return MemberNotificationCreateResponse.builder()
			.notificationId(notification.getId())
			.title(notification.getTitle())
			.content(notification.getContent())
			.timestamp(notification.getCreateAt())
			.isRead(notification.getIsRead())
			.type(notification.getType())
			.referenceId(notification.getReferenceId())
			.build();
	}
}
