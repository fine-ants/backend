package codesquad.fineants.spring.api.watch_list.event;

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
