package codesquad.fineants.spring.api.member.request;

import javax.validation.constraints.NotBlank;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.type.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MemberNotificationSendRequest {
	@NotBlank(message = "필수 정보입니다")
	private String title;
	@NotBlank(message = "필수 정보입니다")
	private String name;
	@NotBlank(message = "필수 정보입니다")
	private String target;
	@NotBlank(message = "필수 정보입니다")
	private String type;
	@NotBlank(message = "필수 정보입니다")
	private String referenceId;
	private String messageId;

	public Notification toEntity(Member member) {
		NotificationType notificationType = NotificationType.from(target);

		if (type.equals("portfolio")) {
			return Notification.portfolioNotification(name, title, notificationType, referenceId, member);
		}
		return Notification.stockTargetPriceNotification(name, Long.valueOf(target), title, referenceId, messageId,
			member);
	}
}
