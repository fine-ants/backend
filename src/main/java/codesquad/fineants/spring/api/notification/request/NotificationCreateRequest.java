package codesquad.fineants.spring.api.notification.request;

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
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class NotificationCreateRequest {
	@NotBlank(message = "필수 정보입니다")
	private String portfolioName;
	@NotBlank(message = "필수 정보입니다")
	private String title;
	@NotNull(message = "필수 정보입니다")
	private NotificationType type;
	@NotBlank(message = "필수 정보입니다")
	private String referenceId;

	public Notification toEntity(Member member) {
		return Notification.portfolioNotification(portfolioName, title, type, referenceId, member);
	}
}
