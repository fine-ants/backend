package codesquad.fineants.domain.kis.properties.kiscodevalue.imple;

import codesquad.fineants.domain.kis.properties.kiscodevalue.KisCodeValue;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public enum CustomerType implements KisCodeValue {
	CORPORATION("B", "법인"),
	INDIVIDUAL("P", "개인");

	private final String code;
	private final String description;

	@Override
	public String getCode() {
		return code;
	}
}
