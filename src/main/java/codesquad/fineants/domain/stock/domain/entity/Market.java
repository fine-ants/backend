package codesquad.fineants.domain.stock.domain.entity;

import java.util.Arrays;

public enum Market {
	KOSPI("KOSPI"), KOSDAQ("KOSDAQ"), KONEX("KONEX"), KOSDAQ_GLOBAL("KOSDAQ GLOBAL"), NONE("NONE");

	private final String name;

	Market(String name) {
		this.name = name;
	}

	public static Market ofMarket(String dbData) {
		return Arrays.stream(values())
			.filter(market -> market.name.equals(dbData))
			.findAny()
			.orElse(NONE);
	}

	public static Market valueByMarketIdCode(String marketIdCode) {
		return switch (marketIdCode) {
			case "STK" -> KOSPI;
			case "KSQ" -> KOSDAQ;
			case "KNX" -> KONEX;
			default -> NONE;
		};
	}

	public String getMarketIdCode() {
		return switch (this) {
			case KOSPI -> "STK";
			case KOSDAQ -> "KSQ";
			case KONEX -> "KNX";
			default -> "";
		};
	}
}
