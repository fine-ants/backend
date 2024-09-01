package co.fineants.api.domain.kis.properties.kiscodevalue.imple;

import co.fineants.api.domain.kis.properties.kiscodevalue.KisCodeValue;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public enum FidOrgAdjPrc implements KisCodeValue {
	ADJUSTED("0", "수정주가 반영"),
	UNADJUSTED("1", "수정주가 미반영");

	private final String code;
	private final String description;

	@Override
	public String getCode() {
		return code;
	}
}
