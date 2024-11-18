package co.fineants.api.domain.portfolio.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class DashboardLineChartResponse {
	@JsonProperty
	private final String time;
	@JsonProperty
	private final Money value;

	@JsonCreator
	public DashboardLineChartResponse(@JsonProperty("time") String time, @JsonProperty("value") Money value) {
		this.time = time;
		this.value = value;
	}

	public static DashboardLineChartResponse of(String time, Expression value) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return new DashboardLineChartResponse(time, value.reduce(bank, to));
	}
}
