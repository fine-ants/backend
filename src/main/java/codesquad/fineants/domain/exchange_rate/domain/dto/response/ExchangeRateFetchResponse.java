package codesquad.fineants.domain.exchange_rate.domain.dto.response;

import java.util.Map;

import codesquad.fineants.domain.common.money.Currency;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateFetchResponse {
	private String base;
	private Map<String, Double> rates;

	public static ExchangeRateFetchResponse krw(Map<String, Double> rates) {
		return new ExchangeRateFetchResponse(Currency.KRW.name(), rates);
	}

	public Double getBy(String code) {
		return rates.getOrDefault(code, 0.0);
	}
}
