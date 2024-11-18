package co.fineants.api.domain.stock.domain.entity;

import java.util.Arrays;

public enum Market {
	KOSPI("KOSPI", "코스피"),
	KOSDAQ("KOSDAQ", "코스닥"),
	KNX("KNX", "코넥스"),
	AGR("AGR", "농축산물파생"),
	BON("BON", "채권파생"),
	CMD("CMD", "일반상품시장"),
	CUR("CUR", "통화파생"),
	ENG("ENG", "에너지파생"),
	EQU("EQU", "주식파생"),
	ETF("ETF", "ETF파생"),
	IRT("IRT", "금리파생"),
	MTL("MTL", "금속파생"),
	SPI("SPI", "주가지수파생"),
	NONE("NONE", "미분류");

	private final String name;
	private final String description;

	Market(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public static Market ofMarket(String dbData) {
		return Arrays.stream(values())
			.filter(market -> market.name.equals(dbData))
			.findAny()
			.orElse(NONE);
	}

	public static Market valueBy(String marketIdCode) {
		return switch (marketIdCode) {
			case "STK" -> KOSPI;
			case "KSQ" -> KOSDAQ;
			case "KNX" -> KNX;
			case "ETF" -> ETF;
			case "AGR" -> AGR;
			case "BON" -> BON;
			case "CMD" -> CMD;
			case "CUR" -> CUR;
			case "ENG" -> ENG;
			case "EQU" -> EQU;
			case "IRT" -> IRT;
			case "MTL" -> MTL;
			case "SPI" -> SPI;
			default -> NONE;
		};
	}

	@Override
	public String toString() {
		return String.format("(name=%s, description=%s)", name, description);
	}
}
