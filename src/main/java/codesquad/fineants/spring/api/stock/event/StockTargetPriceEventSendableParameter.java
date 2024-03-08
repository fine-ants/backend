package codesquad.fineants.spring.api.stock.event;

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
public class StockTargetPriceEventSendableParameter {
	private List<String> tickerSymbols;

	public static StockTargetPriceEventSendableParameter create(List<String> tickerSymbols) {
		return StockTargetPriceEventSendableParameter.builder()
			.tickerSymbols(tickerSymbols)
			.build();
	}
}
