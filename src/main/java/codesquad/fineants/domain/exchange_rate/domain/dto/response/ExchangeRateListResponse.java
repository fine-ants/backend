package codesquad.fineants.domain.exchange_rate.domain.dto.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateListResponse {

	private List<ExchangeRateItem> rates;

	public static ExchangeRateListResponse from(List<ExchangeRateItem> exchangeRateItems) {
		return new ExchangeRateListResponse(exchangeRateItems);
	}
}
