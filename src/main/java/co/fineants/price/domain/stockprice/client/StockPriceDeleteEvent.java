package co.fineants.price.domain.stockprice.client;

import org.springframework.context.ApplicationEvent;

import lombok.ToString;

@ToString
public class StockPriceDeleteEvent extends ApplicationEvent {

	private final String ticker;

	private StockPriceDeleteEvent(String ticker) {
		super(ticker);
		this.ticker = ticker;
	}

	public static StockPriceDeleteEvent from(String ticker) {
		return new StockPriceDeleteEvent(ticker);
	}

	public String getTicker() {
		return ticker;
	}
}
