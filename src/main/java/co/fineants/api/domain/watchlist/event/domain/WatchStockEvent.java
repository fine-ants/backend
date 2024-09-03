package co.fineants.api.domain.watchlist.event.domain;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class WatchStockEvent extends ApplicationEvent {
	private final String tickerSymbol;

	public WatchStockEvent(String tickerSymbol) {
		super(System.currentTimeMillis());
		this.tickerSymbol = tickerSymbol;
	}
}
