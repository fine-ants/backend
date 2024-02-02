package codesquad.fineants.spring.api.firebase;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class NotificationMessage {
	private String recipientToken;
	private String title;
	private String body;
}
