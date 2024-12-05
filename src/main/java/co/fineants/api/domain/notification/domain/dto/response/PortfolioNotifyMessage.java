package co.fineants.api.domain.notification.domain.dto.response;

import java.util.List;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.notification.domain.entity.Notification;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
public class PortfolioNotifyMessage extends NotifyMessage {
	private final String name;
	private final Long portfolioId;

	@Override
	public String getIdToSentHistory() {
		return String.format("portfolioNotification:%s", getReferenceId());
	}

	@Override
	public NotifyMessage withMessageId(List<String> messageIds) {
		return this.toBuilder()
			.messageIds(messageIds)
			.build();
	}

	@Override
	public Notification toEntity(Member member) {
		return Notification.portfolioNotification(name, getTitle(), getType(), getReferenceId(), getLink(), portfolioId,
			member, getMessageIds());
	}
}
