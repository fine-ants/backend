package codesquad.fineants.spring.api.member.request;

import javax.validation.constraints.NotBlank;

import codesquad.fineants.domain.member.Member;
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
public class MemberNotificationSendRequest {
	@NotBlank(message = "필수 정보입니다")
	private String title;
	@NotBlank(message = "필수 정보입니다")
	private String body;
	@NotBlank(message = "필수 정보입니다")
	private String type;
	@NotBlank(message = "필수 정보입니다")
	private String referenceId;

	public Notification toEntity(Member member) {
		return Notification.builder()
			.title(title)
			.content(body)
			.isRead(false)
			.type(type)
			.referenceId(referenceId)
			.member(member)
			.build();
	}
}
