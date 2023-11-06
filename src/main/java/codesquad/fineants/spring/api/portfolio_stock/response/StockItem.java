package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.stock.Stock;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StockItem {
	private String companyName;
	private String tickerSymbol;

	public static StockItem from(Stock stock) {
		return new StockItem(stock.getCompanyName(), stock.getTickerSymbol());
	}
}
