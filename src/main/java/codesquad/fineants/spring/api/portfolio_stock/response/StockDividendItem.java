package codesquad.fineants.spring.api.portfolio_stock.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.stock_dividend.StockDividend;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StockDividendItem {
	private Long dividendId;
	private LocalDateTime dividendMonth;
	private Long dividendAmount;

	public static StockDividendItem from(StockDividend stockDividend) {
		return new StockDividendItem(stockDividend.getId(), stockDividend.getDividendMonth(),
			stockDividend.getDividend());
	}
}
