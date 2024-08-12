package codesquad.fineants.domain.kis.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisQueryParam {
	CONDITION_MARKET_DIVISION_CODE("FID_COND_MRKT_DIV_CODE", "조건 시장 분류 코드"),
	INPUT_STOCK_CODE("FID_INPUT_ISCD", "입력 종목코드");

	private final String paramName;
	private final String korName;
}
