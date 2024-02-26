package codesquad.fineants.spring.api.notification.response;

import java.util.Collections;
import java.util.List;

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
public class NotifyPortfolioMessagesResponse {
	private List<NotifyPortfolioMessageItem> notifications;

	public static NotifyPortfolioMessagesResponse from(
		List<NotifyPortfolioMessageItem> notifications) {
		return NotifyPortfolioMessagesResponse.builder()
			.notifications(notifications)
			.build();
	}

	public static NotifyPortfolioMessagesResponse empty() {
		return NotifyPortfolioMessagesResponse.builder()
			.notifications(Collections.emptyList())
			.build();
	}
}
