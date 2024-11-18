package co.fineants.api.domain.holding.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.stock.domain.entity.Stock;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StockItem {
	@JsonProperty
	private final String companyName;
	@JsonProperty
	private final String tickerSymbol;

	public static StockItem from(Stock stock) {
		return new StockItem(stock.getCompanyName(), stock.getTickerSymbol());
	}

	@Override
	public String toString() {
		return String.format("StockItem(companyName=%s, tickerSymbol=%s)", companyName, tickerSymbol);
	}
}
