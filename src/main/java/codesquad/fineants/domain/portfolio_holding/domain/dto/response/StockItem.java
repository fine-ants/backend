package codesquad.fineants.domain.portfolio_holding.domain.dto.response;

import codesquad.fineants.domain.stock.domain.entity.Stock;
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
