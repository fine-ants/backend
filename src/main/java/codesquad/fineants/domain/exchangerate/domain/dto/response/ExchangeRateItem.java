package codesquad.fineants.domain.exchangerate.domain.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.exchangerate.domain.entity.ExchangeRate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateItem {
	private String code;
	@JsonSerialize(using = Percentage.PercentageDoubleSerializer.class)
	private Percentage rate;

	public static ExchangeRateItem from(ExchangeRate rate) {
		return new ExchangeRateItem(rate.getCode(), rate.getRate());
	}
}

