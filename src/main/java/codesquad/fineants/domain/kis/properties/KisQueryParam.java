package codesquad.fineants.domain.kis.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisQueryParam {
	CONDITION_MARKET_DIVISION_CODE("FID_COND_MRKT_DIV_CODE", "조건 시장 분류 코드"),
	INPUT_STOCK_CODE("FID_INPUT_ISCD", "입력 종목코드"),
	FID_INPUT_DATE_FROM("FID_INPUT_DATE_1", "입력 날짜 (시작)"),
	FID_INPUT_DATE_TO("FID_INPUT_DATE_2", "입력 날짜 (종료)"),
	FID_PERIOD_DIV_CODE("FID_PERIOD_DIV_CODE", "기간분류코드"),
	FID_ORG_ADJ_PRC("FID_ORG_ADJ_PRC", "수정주가 원주가 가격 여부"),
	HIGH_GB("HIGH_GB", "조회일자To"),
	CTS("CTS", "CTS"),
	GB1("GB1", "배당종류"),
	F_DT("F_DT", "조회일자From"),
	T_DT("T_DT", "조회일자To"),
	SHT_CD("SHT_CD", "종목코드");

	private final String paramName;
	private final String korName;
}
