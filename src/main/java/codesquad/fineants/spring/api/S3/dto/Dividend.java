package codesquad.fineants.spring.api.S3.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.google.common.base.Strings;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode(of = {"recordDate", "tickerSymbol"})
public class Dividend {
	private LocalDate recordDate;
	private LocalDate paymentDate;
	private String tickerSymbol;
	private String name;
	private Money dividend;

	public static Dividend from(String[] data) {
		String recordDate = data[0];
		if (Strings.isNullOrEmpty(recordDate)) {
			throw new IllegalArgumentException("recordDate is empty");
		}

		LocalDate paymentDate =
			Strings.isNullOrEmpty(data[1]) ? null : LocalDate.parse(data[1], DateTimeFormatter.BASIC_ISO_DATE);
		String tickerSymbol = data[2];
		String name = data[3];
		Money dividend = Money.from(data[4]);
		return new Dividend(
			LocalDate.parse(data[0], DateTimeFormatter.BASIC_ISO_DATE),
			paymentDate,
			tickerSymbol,
			name,
			dividend
		);
	}

	public boolean containsBy(Map<String, Stock> stockMap) {
		return stockMap.containsKey(tickerSymbol);
	}

	public Stock getStockBy(Map<String, Stock> stockMap) {
		return stockMap.get(tickerSymbol);
	}

	public StockDividend toEntity(Stock stock) {
		return StockDividend.create(
			dividend,
			recordDate,
			paymentDate,
			stock
		);
	}
}
