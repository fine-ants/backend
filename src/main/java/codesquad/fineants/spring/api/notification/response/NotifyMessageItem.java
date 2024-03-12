package codesquad.fineants.spring.api.notification.response;

import codesquad.fineants.domain.notification.type.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class NotifyMessageItem {
	private String title;
	private NotificationType type;
	private String referenceId;
	private String messageId;
	private Long memberId;

	public static NotifyMessageItem empty() {
		return new NotifyMessageItem();
	}
}
