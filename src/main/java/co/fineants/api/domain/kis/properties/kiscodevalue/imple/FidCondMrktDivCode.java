package co.fineants.api.domain.kis.properties.kiscodevalue.imple;

import co.fineants.api.domain.kis.properties.kiscodevalue.KisCodeValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FidCondMrktDivCode implements KisCodeValue {
	STOCK("J"), ETF("ETF"), ETN("ETN"), ELW("W");
	private final String code;
}
