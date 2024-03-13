package codesquad.fineants.spring.api.member.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
public class MemberPortfolioNotificationSendRequest {
	@NotBlank(message = "필수 정보입니다")
	private String title;
	@NotBlank(message = "필수 정보입니다")
	private String name;
	@NotNull(message = "필수 정보입니다")
	private NotificationType type;
	@NotBlank(message = "필수 정보입니다")
	private String referenceId;
	private String messageId;

	public Notification toEntity(Member member) {
		return Notification.portfolioNotification(name, title, type, referenceId, messageId,
			member);
	}
}
