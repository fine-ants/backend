package co.fineants.api.domain.kis.properties;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import co.fineants.api.domain.kis.properties.kiscodevalue.KisCodeValue;

public class KisQueryParamBuilder {
	private final Map<KisQueryParam, String> headers = new EnumMap<>(KisQueryParam.class);

	public static KisQueryParamBuilder builder() {
		return new KisQueryParamBuilder();
	}

	public KisQueryParamBuilder add(KisQueryParam header, String value) {
		headers.put(header, value);
		return this;
	}

	public KisQueryParamBuilder add(KisQueryParam header, KisCodeValue value) {
		headers.put(header, value.getCode());
		return this;
	}

	public MultiValueMap<String, String> build() {
		return headers.entrySet().stream()
			.collect(LinkedMultiValueMap::new,
				(map, entry) -> map.add(entry.getKey().getParamName(), entry.getValue()),
				LinkedMultiValueMap::addAll);
	}
}
