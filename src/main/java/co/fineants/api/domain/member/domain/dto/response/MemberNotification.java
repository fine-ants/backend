package co.fineants.api.domain.member.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.NotificationBody;
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
	private NotificationBody body;
	private LocalDateTime timestamp;
	private Boolean isRead;
	private String type;
	private String referenceId;

	public static MemberNotification from(Notification notification) {
		return MemberNotification.builder()
			.notificationId(notification.getId())
			.title(notification.getTitle())
			.body(notification.getBody())
			.timestamp(notification.getCreateAt())
			.isRead(notification.getIsRead())
			.type(notification.getType().getCategory())
			.referenceId(notification.getReferenceId())
			.build();
	}
}
