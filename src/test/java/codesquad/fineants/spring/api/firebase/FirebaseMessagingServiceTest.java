package codesquad.fineants.spring.api.firebase;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

@ActiveProfiles("test")
@SpringBootTest
class FirebaseMessagingServiceTest {

	@Autowired
	private FirebaseMessagingService firebaseMessagingService;

	@MockBean
	private FirebaseMessaging firebaseMessaging;

	@DisplayName("클라이언트에게 토큰을 이용하여 알림 메시지를 푸시한다")
	@Test
	void sendNotification() throws FirebaseMessagingException {
		// given
		NotificationMessage notificationMessage = NotificationMessage.builder()
			.recipientToken("token")
			.title("test title")
			.body("message content")
			.build();

		BDDMockito.given(firebaseMessaging.send(ArgumentMatchers.any(Message.class)))
			.willReturn("1");

		// when
		String result = firebaseMessagingService.sendNotification(notificationMessage);

		// then
		assertThat(result).isEqualTo("Success Sending Notification");
	}
}
