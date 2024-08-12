package codesquad.fineants.domain.kis.properties.kiscodevalue.imple;

import codesquad.fineants.domain.kis.properties.kiscodevalue.KisCodeValue;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public enum PrdtTypeCd implements KisCodeValue {
	STOCK("300", "주식"),
	FUTURES_OPTIONS("301", "선물옵션"),
	BONDS("302", "채권"),
	NASDAQ("512", "미국 나스닥"),
	NYSE("513", "미국 뉴욕"),
	AMEX("529", "미국 아멕스"),
	JAPAN("515", "일본"),
	HONG_KONG("501", "홍콩"),
	HONG_KONG_CNY("543", "홍콩 CNY"),
	HONG_KONG_USD("558", "홍콩 USD"),
	VIETNAM_HANOI("507", "베트남 하노이"),
	VIETNAM_HO_CHI_MINH("508", "베트남 호치민"),
	CHINA_SHANGHAI_A("551", "중국 상해 A"),
	CHINA_SHENZHEN_A("552", "중국 심천 A");

	private final String code;
	private final String description;

	@Override
	public String getCode() {
		return code;
	}
}
