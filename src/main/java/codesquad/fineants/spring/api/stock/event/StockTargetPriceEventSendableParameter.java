package codesquad.fineants.spring.api.stock.event;

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
public class StockTargetPriceEventSendableParameter {
	private String tickerSymbol;
	private Long currentPrice;

	public static StockTargetPriceEventSendableParameter create(String tickerSymbol, Long currentPrice) {
		return StockTargetPriceEventSendableParameter.builder()
			.tickerSymbol(tickerSymbol)
			.currentPrice(currentPrice)
			.build();
	}
}
