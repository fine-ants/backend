package codesquad.fineants.domain.member.domain.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.notification.domain.entity.Notification;
import codesquad.fineants.domain.notification.domain.entity.type.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@ToString
public class MemberPortfolioNotificationSendRequest {
	@NotBlank(message = "필수 정보입니다")
	private String title;
	@NotBlank(message = "필수 정보입니다")
	private String name;
	@NotNull(message = "필수 정보입니다")
	private NotificationType type;
	@NotBlank(message = "필수 정보입니다")
	private String referenceId;
	@NotBlank(message = "필수 정보입니다")
	private String link;

	public static MemberPortfolioNotificationSendRequest create(String title, String name, NotificationType type,
		String referenceId, String link) {
		return MemberPortfolioNotificationSendRequest.builder()
			.title(title)
			.name(name)
			.type(type)
			.referenceId(referenceId)
			.link(link)
			.build();
	}

	public Notification toEntity(Member member) {
		return Notification.portfolio(name, title, type, referenceId, link, member);
	}
}
