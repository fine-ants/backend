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

	public static Dividend parse(String[] data) {
		String recordDate = data[0];
		if (Strings.isNullOrEmpty(recordDate)) {
			throw new IllegalArgumentException("recordDate is empty");
		}

		LocalDate paymentDate =
			Strings.isNullOrEmpty(data[1]) ? null : LocalDate.parse(data[1], DateTimeFormatter.BASIC_ISO_DATE);
		String tickerSymbol = formatTickerSymbol(data[2]);
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

	private static String formatTickerSymbol(String tickerSymbol) {
		StringBuilder sb = new StringBuilder(tickerSymbol);
		while (sb.length() < 6) {
			sb.insert(0, "0");
		}
		return sb.toString();
	}

	public static Dividend create(LocalDate recordDate, LocalDate paymentDate, String tickerSymbol, String name,
		Money dividend) {
		return new Dividend(recordDate, paymentDate, tickerSymbol, name, dividend);
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

	public String toCsv() {
		String formattedPaymentDate = paymentDate == null ? "" : paymentDate.format(DateTimeFormatter.BASIC_ISO_DATE);
		return String.join(",",
			recordDate.format(DateTimeFormatter.BASIC_ISO_DATE),
			formattedPaymentDate,
			tickerSymbol,
			name,
			dividend.toString());
	}
}
