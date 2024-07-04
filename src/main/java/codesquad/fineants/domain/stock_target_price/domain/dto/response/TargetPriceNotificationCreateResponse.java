package codesquad.fineants.domain.stock_target_price.domain.dto.response;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.stock_target_price.domain.entity.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;
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
	private Long stockTargetPriceId;
	private Long targetPriceNotificationId;
	private String tickerSymbol;
	private Money targetPrice;

	public static TargetPriceNotificationCreateResponse from(StockTargetPrice stockTargetPrice,
		TargetPriceNotification targetPriceNotification) {
		return TargetPriceNotificationCreateResponse.builder()
			.stockTargetPriceId(stockTargetPrice.getId())
			.targetPriceNotificationId(targetPriceNotification.getId())
			.tickerSymbol(stockTargetPrice.getStock().getTickerSymbol())
			.targetPrice(targetPriceNotification.getTargetPrice())
			.build();
	}
}
