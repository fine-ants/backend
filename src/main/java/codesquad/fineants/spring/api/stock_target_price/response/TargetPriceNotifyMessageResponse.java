package codesquad.fineants.spring.api.stock_target_price.response;

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
public class TargetPriceNotifyMessageResponse {
	private List<TargetPriceNotifyMessageItem> notifications;

	public static TargetPriceNotifyMessageResponse from(List<TargetPriceNotifyMessageItem> notifications) {
		return TargetPriceNotifyMessageResponse.builder()
			.notifications(notifications)
			.build();
	}

	public static TargetPriceNotifyMessageResponse empty() {
		return TargetPriceNotifyMessageResponse.builder()
			.notifications(Collections.emptyList())
			.build();
	}
}
