package codesquad.fineants.spring.api.stock.response;

import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
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

	public static TargetPriceNotificationCreateResponse from(StockTargetPrice stockTargetPrice) {
		return TargetPriceNotificationCreateResponse.builder()
			.targetPriceNotificationId(stockTargetPrice.getId())
			.tickerSymbol(stockTargetPrice.getStock().getTickerSymbol())
			.targetPrice(stockTargetPrice.getTargetPrice())
			.build();
	}
}
