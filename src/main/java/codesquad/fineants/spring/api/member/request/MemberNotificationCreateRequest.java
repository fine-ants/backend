package codesquad.fineants.spring.api.member.request;

import javax.validation.constraints.NotBlank;

import codesquad.fineants.domain.notification.Notification;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberNotificationCreateRequest {
	@NotBlank(message = "필수 정보입니다")
	private String title;
	@NotBlank(message = "필수 정보입니다")
	private String content;
	@NotBlank(message = "필수 정보입니다")
	private String type;
	@NotBlank(message = "필수 정보입니다")
	private String referenceId;

	public Notification toEntity() {
		return Notification.builder()
			.title(title)
			.content(content)
			.isRead(false)
			.type(type)
			.referenceId(referenceId)
			.build();
	}
}
