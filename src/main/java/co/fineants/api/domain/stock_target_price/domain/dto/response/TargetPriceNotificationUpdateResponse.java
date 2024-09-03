package co.fineants.api.domain.stock_target_price.domain.dto.response;

import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
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
public class TargetPriceNotificationUpdateResponse {
	private Long stockTargetPriceId;
	private String tickerSymbol;
	private Boolean isActive;

	public static TargetPriceNotificationUpdateResponse from(StockTargetPrice stockTargetPrice) {
		return TargetPriceNotificationUpdateResponse.builder()
			.stockTargetPriceId(stockTargetPrice.getId())
			.tickerSymbol(stockTargetPrice.getStock().getTickerSymbol())
			.isActive(stockTargetPrice.getIsActive())
			.build();
	}
}
