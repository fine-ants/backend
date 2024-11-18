package co.fineants.api.domain.stock_target_price.domain.dto.response;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TargetPriceNotifyMessageResponse implements NotifyMessageResponse {
	@JsonProperty
	private final List<NotifyMessageItem> notifications;

	public static TargetPriceNotifyMessageResponse create(List<NotifyMessageItem> notifyMessageItems) {
		return new TargetPriceNotifyMessageResponse(notifyMessageItems);
	}

	public static TargetPriceNotifyMessageResponse empty() {
		return new TargetPriceNotifyMessageResponse(Collections.emptyList());
	}

	@Override
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public boolean isEmpty() {
		return notifications.isEmpty();
	}

	@Override
	public String toString() {
		return String.format("종목 지정가 알림 메시지 결과(notifications=%s)", notifications);
	}
}
