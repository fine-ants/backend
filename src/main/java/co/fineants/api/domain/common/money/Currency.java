package co.fineants.api.domain.common.money;

import lombok.Getter;

@Getter
public enum Currency {
	USD("$"),
	CHF("FR"),
	KRW("₩");

	private final String symbol;

	Currency(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return symbol;
	}
}
