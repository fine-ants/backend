package co.fineants.api.domain.notification.domain.dto.response;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioNotifyMessagesResponse implements NotifyMessageResponse {
	@JsonProperty
	private final List<NotifyMessageItem> notifications;

	public static PortfolioNotifyMessagesResponse create(
		List<NotifyMessageItem> items) {
		return new PortfolioNotifyMessagesResponse(items);
	}

	public static PortfolioNotifyMessagesResponse empty() {
		return new PortfolioNotifyMessagesResponse(Collections.emptyList());
	}

	@Override
	public boolean isEmpty() {
		return notifications.isEmpty();
	}
}
