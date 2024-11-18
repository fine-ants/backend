package co.fineants.api.domain.exchangerate.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.exchangerate.domain.entity.ExchangeRate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateItem {
	@JsonProperty
	private final String code;
	@JsonSerialize(using = Percentage.PercentageDoubleSerializer.class)
	@JsonProperty
	private final Percentage rate;

	public static ExchangeRateItem from(ExchangeRate rate) {
		return new ExchangeRateItem(rate.getCode(), rate.getRate());
	}
}

