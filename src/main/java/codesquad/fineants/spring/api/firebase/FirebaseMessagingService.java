package codesquad.fineants.spring.api.firebase;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseMessagingService {
	private final FirebaseMessaging firebaseMessaging;

	public String sendNotification(NotificationMessage notificationMessage) {
		Notification notification = Notification
			.builder()
			.setTitle(notificationMessage.getTitle())
			.setBody(notificationMessage.getBody())
			.build();

		Message message = Message
			.builder()
			.setToken(notificationMessage.getRecipientToken())
			.setNotification(notification)
			.build();

		try {
			String messageId = firebaseMessaging.send(message);
			log.info("푸시 알림 전송, messageId={}", messageId);
			return "Success Sending Notification";
		} catch (FirebaseMessagingException e) {
			log.error(e.getMessage(), e);
			return "Error Sending Notification";
		}
	}
}
