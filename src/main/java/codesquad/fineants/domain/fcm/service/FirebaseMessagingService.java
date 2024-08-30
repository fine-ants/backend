package codesquad.fineants.domain.fcm.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.SendResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseMessagingService {
	private final FirebaseMessaging firebaseMessaging;

	public Optional<String> send(Message message) {
		try {
			String messageId = firebaseMessaging.send(message);
			log.info("푸시 알림 전송 결과 : messageId={}", messageId);
			return Optional.ofNullable(messageId);
		} catch (FirebaseMessagingException e) {
			log.error("푸시 알림 전송 실패 : {}", e.getMessage());
			return Optional.empty();
		}
	}

	public List<String> send(List<Message> messages) {
		try {
			BatchResponse batchResponse = firebaseMessaging.sendAll(messages);
			return batchResponse.getResponses().stream()
				.map(SendResponse::getMessageId)
				.toList();
		} catch (FirebaseMessagingException e) {
			log.error("푸시 알림 전송 실패 : {}", e.getMessage());
			return Collections.emptyList();
		}
	}
}
