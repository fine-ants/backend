package codesquad.fineants.domain.kis.properties.kiscodevalue.imple;

import codesquad.fineants.domain.kis.properties.kiscodevalue.KisCodeValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GB1 implements KisCodeValue {
	TOTAL_DIVIDEND("0", "배당 전체"),
	FINAL_DIVIDEND("1", "결산 배당"),
	INTERIM_DIVIDEND("2", "중간 배당");

	private final String code;
	private final String description;

	@Override
	public String getCode() {
		return code;
	}
}
