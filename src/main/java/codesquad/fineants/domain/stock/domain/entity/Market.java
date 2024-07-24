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

	public static Market valueOf(String kospi200ItemYn, String mketIdCd) {
		Market market = NONE;
		if ("Y".equals(kospi200ItemYn)) {
			market = Market.KOSPI;
		} else if ("KSQ".equals(mketIdCd)) {
			market = Market.KOSDAQ;
		} else if ("KNX".equals(mketIdCd)) {
			market = Market.KONEX;
		}
		return market;
	}
}
