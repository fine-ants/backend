package codesquad.fineants.domain.stock_target_price.domain.dto.response;

import java.util.Collections;
import java.util.List;

import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessageItem;
import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessageResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class TargetPriceNotifyMessageResponse implements NotifyMessageResponse {
	private List<NotifyMessageItem> notifications;

	public static TargetPriceNotifyMessageResponse create(List<NotifyMessageItem> notifyMessageItems) {
		return new TargetPriceNotifyMessageResponse(notifyMessageItems);
	}

	public static TargetPriceNotifyMessageResponse empty() {
		return new TargetPriceNotifyMessageResponse(Collections.emptyList());
	}

	@Override
	public boolean isEmpty() {
		return notifications.isEmpty();
	}
}
