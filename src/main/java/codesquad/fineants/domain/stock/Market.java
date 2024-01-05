package codesquad.fineants.domain.stock;

import java.util.Arrays;

import codesquad.fineants.spring.api.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;

public enum Market {
	KOSPI, KOSDAQ, KONEX, KOSDAQ_GLOBAL;

	public static Market ofMarket(String dbData) {
		return Arrays.stream(values())
			.filter(market -> market.name().equals(dbData))
			.findAny()
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_MARKET,
				String.format("%s 종류는 찾을 수 없습니다", dbData)));
	}
}
