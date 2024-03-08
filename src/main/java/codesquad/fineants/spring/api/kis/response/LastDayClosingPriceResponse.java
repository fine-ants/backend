package codesquad.fineants.spring.api.kis.response;

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
public class LastDayClosingPriceResponse {
	private String tickerSymbol;
	private Long closingPrice;

	public static LastDayClosingPriceResponse create(String tickerSymbol, Long closingPrice) {
		return new LastDayClosingPriceResponse(tickerSymbol, closingPrice);
	}
}
