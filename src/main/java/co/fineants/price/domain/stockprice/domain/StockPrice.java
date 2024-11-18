package co.fineants.price.domain.stockprice.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "ticker")
@Getter
public class StockPrice {
	private final String ticker;
	private final LocalDateTime expiration;

	public static StockPrice newInstance(String ticker) {
		return newInstance(ticker, LocalDateTime.now().plusMinutes(1));
	}

	public static StockPrice newInstance(String ticker, LocalDateTime expiration) {
		return new StockPrice(ticker, expiration);
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiration);
	}

	@Override
	public String toString() {
		return String.format("StockPrice(ticker=%s, expiration=%s)", ticker,
			expiration.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
	}
}
