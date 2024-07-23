package codesquad.fineants.domain.notification.domain.dto.response;

import codesquad.fineants.domain.notification.domain.entity.type.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PortfolioNotifyMessage extends NotifyMessage {
	private final String name;

	@Builder
	public PortfolioNotifyMessage(String title, String content, NotificationType type, String referenceId,
		Long memberId, String token, String link, String name) {
		super(title, content, type, referenceId, memberId, token, link);
		this.name = name;
	}

	public static NotifyMessage create(String title, String content, NotificationType type, String referenceId,
		Long memberId, String token, String link, String name) {
		return PortfolioNotifyMessage.builder()
			.title(title)
			.content(content)
			.type(type)
			.referenceId(referenceId)
			.memberId(memberId)
			.token(token)
			.link(link)
			.name(name)
			.build();
	}

	@Override
	public String getIdToSentHistory() {
		return String.format("portfolioNotification:%s", getReferenceId());
	}
}
