package codesquad.fineants.spring.api.stock_target_price.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TargetPriceNotificationCreateRequest {

	@NotNull(message = "필수 정보입니다")
	private String tickerSymbol;

	@NotNull(message = "필수 정보입니다")
	@PositiveOrZero(message = "지정가는 0포함 양수여야 합니다")
	private Long targetPrice;

	public TargetPriceNotification toEntity(StockTargetPrice stockTargetPrice) {
		return TargetPriceNotification.builder()
			.targetPrice(targetPrice)
			.stockTargetPrice(stockTargetPrice)
			.build();
	}
}
