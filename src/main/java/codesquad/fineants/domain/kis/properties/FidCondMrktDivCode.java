package codesquad.fineants.domain.kis.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FidCondMrktDivCode implements KisCodeValue {
	STOCK("J"), ETF("ETF"), ETN("ETN"), ELW("W");
	private final String code;
}
