package co.fineants.price.domain.stockprice.domain;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "ticker")
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
}
