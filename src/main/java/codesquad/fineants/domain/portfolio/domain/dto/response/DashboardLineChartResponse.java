package codesquad.fineants.domain.portfolio.domain.dto.response;

import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DashboardLineChartResponse {
	private String time;
	private Money value;

	public static DashboardLineChartResponse of(String time, Expression value) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return new DashboardLineChartResponse(time, value.reduce(bank, to));
	}
}
