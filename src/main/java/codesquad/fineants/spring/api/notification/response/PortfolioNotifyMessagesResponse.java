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
@Builder(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioNotifyMessagesResponse {
	private List<PortfolioNotifyMessageItem> notifications;

	public static PortfolioNotifyMessagesResponse create(
		List<PortfolioNotifyMessageItem> items) {
		return PortfolioNotifyMessagesResponse.builder()
			.notifications(items)
			.build();
	}

	public static PortfolioNotifyMessagesResponse empty() {
		return PortfolioNotifyMessagesResponse.builder()
			.notifications(Collections.emptyList())
			.build();
	}
}
