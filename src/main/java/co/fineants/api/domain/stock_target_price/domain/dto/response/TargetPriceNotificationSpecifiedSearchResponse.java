package co.fineants.api.domain.stock_target_price.domain.dto.response;

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
public class TargetPriceNotificationSpecifiedSearchResponse {
	private List<TargetPriceNotificationSpecificItem> targetPrices;

	public static TargetPriceNotificationSpecifiedSearchResponse from(
		List<TargetPriceNotificationSpecificItem> targetPrices
	) {
		return TargetPriceNotificationSpecifiedSearchResponse.builder()
			.targetPrices(targetPrices)
			.build();
	}
}
