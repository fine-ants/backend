package codesquad.fineants.domain.notification.domain.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
@EqualsAndHashCode(of = "notifyMessage")
public class SentNotifyMessage {
	private NotifyMessage notifyMessage;
	private String messageId;

	public static SentNotifyMessage create(NotifyMessage notifyMessage, String messageId) {
		return SentNotifyMessage.builder()
			.notifyMessage(notifyMessage)
			.messageId(messageId)
			.build();
	}
}
