package co.fineants.api.infra.s3.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.common.base.Strings;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.Stock;
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
	private Money amount;

	public static Dividend parse(String[] data) {
		String recordDate = data[0];
		if (Strings.isNullOrEmpty(recordDate)) {
			throw new IllegalArgumentException("recordDate is empty");
		}

		LocalDate paymentDate =
			Strings.isNullOrEmpty(data[1]) ? null : LocalDate.parse(data[1], DateTimeFormatter.BASIC_ISO_DATE);
		String tickerSymbol = formatTickerSymbol(data[2]);
		String name = data[3];
		Money dividend = Money.won(data[4]);
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

	public StockDividend toEntity(Stock stock) {
		return StockDividend.create(
			amount,
			recordDate,
			paymentDate,
			stock
		);
	}
}
