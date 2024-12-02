package co.fineants.api.domain.notification.domain.dto.response;

import java.util.List;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PortfolioNotifyMessage extends NotifyMessage {
	private final String name;

	@Builder(access = AccessLevel.PRIVATE)
	private PortfolioNotifyMessage(String title, String content, NotificationType type, String referenceId,
		Long memberId, String token, String link, String name, List<String> messageIds) {
		super(title, content, type, referenceId, memberId, token, link, messageIds);
		this.name = name;
	}

	public static NotifyMessage create(String title, String content, NotificationType type, String referenceId,
		Long memberId, String token, String link, String name, List<String> messageIds) {
		return PortfolioNotifyMessage.builder()
			.title(title)
			.content(content)
			.type(type)
			.referenceId(referenceId)
			.memberId(memberId)
			.token(token)
			.link(link)
			.name(name)
			.messageIds(messageIds)
			.build();
	}

	@Override
	public String getIdToSentHistory() {
		return String.format("portfolioNotification:%s", getReferenceId());
	}

	@Override
	public NotifyMessage withMessageId(List<String> messageIds) {
		return portfolio(getTitle(), getContent(), getType(), getReferenceId(), getMemberId(), getToken(), getLink(),
			getName(), messageIds);
	}

	@Override
	public Notification toEntity(Member member) {
		return Notification.portfolio(getName(), getTitle(), getType(), getReferenceId(), getLink(), member);
	}
}
