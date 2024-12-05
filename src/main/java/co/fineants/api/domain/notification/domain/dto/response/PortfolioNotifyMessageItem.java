package co.fineants.api.domain.notification.domain.dto.response;

import java.util.List;

import co.fineants.api.domain.notification.domain.dto.response.save.NotificationSaveResponse;
import co.fineants.api.domain.notification.domain.entity.Notification;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@ToString
public class PortfolioNotifyMessageItem extends NotifyMessageItem {
	private String name;

	public static NotifyMessageItem from(NotificationSaveResponse response, String messageId) {
		return response.toNotifyMessageItemWith(List.of(messageId));
	}

	public static NotifyMessageItem from(Notification notification) {
		return NotifyMessageItem.portfolioNotifyMessageItem(
			notification.getId(),
			notification.getIsRead(),
			notification.getTitle(),
			notification.getContent(),
			notification.getType(),
			notification.getReferenceId(),
			notification.getMember().getId(),
			notification.getLink(),
			notification.getName(),
			notification.getMessageIds()
		);
	}
}
