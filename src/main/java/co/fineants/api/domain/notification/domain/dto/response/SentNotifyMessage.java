package co.fineants.api.domain.notification.domain.dto.response;

import org.apache.logging.log4j.util.Strings;

import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.domain.notification.domain.dto.request.NotificationSaveRequest;
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

	public boolean hasMessageId() {
		return !Strings.isBlank(messageId);
	}

	public void deleteToken(FcmService service) {
		notifyMessage.deleteTokenBy(service);
	}

	public NotificationSaveRequest toNotificationSaveRequest() {
		return notifyMessage.toNotificationSaveRequest();
	}
}
