package codesquad.fineants.spring.api.notification.request;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.spring.api.notification.response.NotifyMessage;
import codesquad.fineants.spring.api.notification.response.PortfolioNotifyMessage;
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
public class PortfolioNotificationRequest {
	private String name;
	private String title;
	private NotificationType type;
	private String referenceId;
	private String link;
	private Long memberId;

	public static PortfolioNotificationRequest from(NotifyMessage message) {
		PortfolioNotifyMessage portfolioNotifyMessage = (PortfolioNotifyMessage)message;
		return PortfolioNotificationRequest.builder()
			.name(portfolioNotifyMessage.getName())
			.title(portfolioNotifyMessage.getTitle())
			.type(portfolioNotifyMessage.getType())
			.referenceId(portfolioNotifyMessage.getReferenceId())
			.link(portfolioNotifyMessage.getLink())
			.memberId(portfolioNotifyMessage.getMemberId())
			.build();
	}

	public Notification toEntity(Member member) {
		return Notification.portfolio(name, title, type, referenceId, link, member);
	}
}
