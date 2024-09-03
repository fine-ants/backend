package co.fineants.api.domain.holding.domain.dto.response;

import co.fineants.api.domain.stock.domain.entity.Stock;
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
