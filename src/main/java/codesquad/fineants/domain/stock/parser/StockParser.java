package codesquad.fineants.domain.stock.parser;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;

@Component
public class StockParser {
	public Stock parse(String[] data) {
		try {
			String stockCode = data[0];
			String tickerSymbol = data[1];
			String companyName = data[2];
			String companyNameEng = data[3];
			Market market = Market.ofMarket(data[4]);
			String sector = "none";
			if (data.length >= 6) {
				sector = data[5];
			}
			return Stock.of(tickerSymbol, companyName, companyNameEng, stockCode, sector, market);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("out of index, data:" + Arrays.toString(data));
		}
	}
}
