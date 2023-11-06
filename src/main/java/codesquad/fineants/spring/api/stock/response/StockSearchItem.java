package codesquad.fineants.spring.api.stock.response;

import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StockSearchItem {
	private String stockCode;
	private String tickerSymbol;
	private String companyName;
	private String companyNameEng;
	private Market market;

	public static StockSearchItem from(Stock stock) {
		return new StockSearchItem(stock.getStockCode(), stock.getTickerSymbol(), stock.getCompanyName(),
			stock.getCompanyNameEng(), stock.getMarket());
	}
}
