package codesquad.fineants.domain.stock.domain.entity;

import java.util.Arrays;

public enum Market {
	KOSPI, KOSDAQ, KONEX, KOSDAQ_GLOBAL, NONE;

	public static Market ofMarket(String dbData) {
		return Arrays.stream(values())
			.filter(market -> market.name().equals(dbData))
			.findAny()
			.orElse(NONE);
	}
}
