package co.fineants.api.domain.stock_target_price.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
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
public class TargetPriceItem {
	private Long notificationId;
	private Money targetPrice;
	private LocalDateTime dateAdded;

	public static TargetPriceItem from(TargetPriceNotification targetPriceNotification) {
		return TargetPriceItem.builder()
			.notificationId(targetPriceNotification.getId())
			.targetPrice(targetPriceNotification.getTargetPrice())
			.dateAdded(targetPriceNotification.getCreateAt())
			.build();
	}
}
