package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.common.money.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioDividendChartItem {
	private int month;
	private Money amount;

	public static PortfolioDividendChartItem empty(int month) {
		return create(month, Money.zero());
	}

	public static PortfolioDividendChartItem create(int month, Money amount) {
		return new PortfolioDividendChartItem(
			month,
			amount
		);
	}
}
