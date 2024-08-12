package codesquad.fineants.domain.kis.properties.kiscodevalue.imple;

import codesquad.fineants.domain.kis.properties.kiscodevalue.KisCodeValue;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public enum FidPeriodDivCode implements KisCodeValue {
	DAILY_LAST_30_TRADING_DAYS("D", "최근 30거래일"),
	WEEKLY_LAST_30_WEEKS("W", "최근 30주"),
	MONTHLY_LAST_30_MONTHS("M", "최근 30개월");

	private final String code;
	private final String description;

	@Override
	public String getCode() {
		return code;
	}
}
