package codesquad.fineants.domain.notification.domain.dto.response;

import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioNotifyMessagesResponse implements NotifyMessageResponse {
	private List<NotifyMessageItem> notifications;

	public static PortfolioNotifyMessagesResponse create(
		List<NotifyMessageItem> items) {
		return new PortfolioNotifyMessagesResponse(items);
	}

	public static PortfolioNotifyMessagesResponse empty() {
		return new PortfolioNotifyMessagesResponse(Collections.emptyList());
	}
}
