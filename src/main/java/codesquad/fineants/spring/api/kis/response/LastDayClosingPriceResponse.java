package codesquad.fineants.spring.api.kis.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LastDayClosingPriceResponse {
	private String tickerSymbol;
	private long price;

	public static LastDayClosingPriceResponse of(String tickerSymbol, long price) {
		return new LastDayClosingPriceResponse(tickerSymbol, price);
	}
}
