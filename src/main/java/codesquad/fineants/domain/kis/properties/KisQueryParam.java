package codesquad.fineants.domain.kis.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisQueryParam {
	CONDITION_MARKET_DIVISION_CODE("fid_cond_mrkt_div_code", "조건 시장 분류 코드"),
	INPUT_STOCK_CODE("fid_input_iscd", "입력 종목코드");

	private final String paramName;
	private final String korName;
}
