package codesquad.fineants.spring.api.stock_target_price.response;

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
public class TargetPriceNotificationCreateResponse {
	private Long targetPriceNotificationId;
	private String tickerSymbol;
	private Long targetPrice;

	public static TargetPriceNotificationCreateResponse from(StockTargetPrice stockTargetPrice,
		TargetPriceNotification targetPriceNotification) {
		return TargetPriceNotificationCreateResponse.builder()
			.targetPriceNotificationId(targetPriceNotification.getId())
			.tickerSymbol(stockTargetPrice.getStock().getTickerSymbol())
			.targetPrice(targetPriceNotification.getTargetPrice().getAmount().longValue())
			.build();
	}
}
