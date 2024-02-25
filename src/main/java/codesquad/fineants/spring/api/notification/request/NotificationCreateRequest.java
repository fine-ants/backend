package codesquad.fineants.spring.api.notification.request;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.Notification;
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
@Builder
@ToString
public class NotificationCreateRequest {
	private String portfolioName;
	private String title;
	private NotificationType type;
	private String referenceId;

	public Notification toEntity(Member member) {
		return Notification.portfolioNotification(portfolioName, title, type, referenceId, member);
	}
}
