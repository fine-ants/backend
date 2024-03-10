package codesquad.fineants.spring.api.kis.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CurrentPriceResponse {
	private String tickerSymbol;
	private Long currentPrice;

	public static CurrentPriceResponse create(String tickerSymbol, Long currentPrice) {
		return new CurrentPriceResponse(tickerSymbol, currentPrice);
	}
}
