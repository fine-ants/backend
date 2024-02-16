package codesquad.fineants.spring.api.stock.response;

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
public class TargetPriceNotificationSearchResponse {
	private List<TargetPriceNotificationSearchItem> stocks;

	public static TargetPriceNotificationSearchResponse from(List<TargetPriceNotificationSearchItem> stocks) {
		return TargetPriceNotificationSearchResponse.builder()
			.stocks(stocks)
			.build();
	}
}
