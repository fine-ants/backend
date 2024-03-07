package codesquad.fineants.spring.api.stock.response;

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
public class TargetPriceNotificationSendResponse {
	private List<TargetPriceNotificationSendItem> notifications;

	public static TargetPriceNotificationSendResponse from(List<TargetPriceNotificationSendItem> notifications) {
		return TargetPriceNotificationSendResponse.builder()
			.notifications(notifications)
			.build();
	}

	public static TargetPriceNotificationSendResponse empty() {
		return TargetPriceNotificationSendResponse.builder()
			.notifications(Collections.emptyList())
			.build();
	}
}
