package codesquad.fineants.domain.stock_target_price.domain.dto.response;

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
public class TargetPriceNotificationDeleteResponse {
	private List<Long> deletedIds;

	public static TargetPriceNotificationDeleteResponse from(List<Long> targetPriceNotificationIds) {
		return TargetPriceNotificationDeleteResponse.builder()
			.deletedIds(targetPriceNotificationIds)
			.build();
	}
}
