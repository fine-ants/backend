package codesquad.fineants.spring.api.kis.response;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CurrentPriceResponse {
	private final String tickerSymbol;
	private final long currentPrice;

	public CurrentPriceResponse(String tickerSymbol, long currentPrice) {
		this.tickerSymbol = tickerSymbol;
		this.currentPrice = currentPrice;
	}
}
