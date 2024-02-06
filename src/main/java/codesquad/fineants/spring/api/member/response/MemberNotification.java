package codesquad.fineants.spring.api.member.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.notification.Notification;
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
@EqualsAndHashCode(of = "notificationId")
@ToString
@Builder
public class MemberNotification {
	private Long notificationId;
	private String title;
	private String content;
	private LocalDateTime timestamp;
	private Boolean isRead;
	private String type;
	private String referenceId;

	public static MemberNotification from(Notification notification) {
		return MemberNotification.builder()
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
