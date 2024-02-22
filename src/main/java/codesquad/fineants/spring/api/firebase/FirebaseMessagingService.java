package codesquad.fineants.spring.api.firebase;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseMessagingService {
	private final FirebaseMessaging firebaseMessaging;

	public Optional<String> sendNotification(Message message) {
		try {
			String messageId = firebaseMessaging.send(message);
			log.info("푸시 알림 전송 결과 : messageId={}", messageId);
			return Optional.ofNullable(messageId);
		} catch (FirebaseMessagingException ignored) {
			log.info("푸시 알림 전송 실패");
			return Optional.empty();
		}
	}
}
