package co.fineants.api.domain.exchangerate.domain.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateListResponse {

	@JsonProperty
	private final List<ExchangeRateItem> rates;

	public static ExchangeRateListResponse from(List<ExchangeRateItem> exchangeRateItems) {
		return new ExchangeRateListResponse(exchangeRateItems);
	}
}
