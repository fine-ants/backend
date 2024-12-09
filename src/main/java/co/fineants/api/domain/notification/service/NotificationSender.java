package co.fineants.api.domain.notification.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import com.google.firebase.messaging.Message;

import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.domain.fcm.service.FirebaseMessagingService;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationSender {

	private final FirebaseMessagingService firebaseMessagingService;
	private final FcmService fcmService;

	public List<NotifyMessage> send(List<NotifyMessage> data) {
		Map<NotifyMessage, List<String>> messageIdsMap = new HashMap<>();
		for (NotifyMessage notifyMessage : data) {
			Message message = notifyMessage.toMessage();
			String messageId = firebaseMessagingService.send(message).orElse(Strings.EMPTY);
			messageIdsMap.computeIfAbsent(notifyMessage, key -> new ArrayList<>())
				.add(messageId);
		}
		return messageIdsMap.entrySet().stream()
			.map(entry -> entry.getKey().withMessageId(entry.getValue()))
			.toList();
	}

	public void deleteTokensForFailedMessagesIn(List<NotifyMessage> data) {
		data.stream()
			.filter(NotifyMessage::hasNotMessageId)
			.forEach(notifyMessage -> notifyMessage.deleteTokenBy(fcmService));
	}
}
