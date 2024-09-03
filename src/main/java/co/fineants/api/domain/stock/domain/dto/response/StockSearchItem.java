package co.fineants.api.domain.stock.domain.dto.response;

import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
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
