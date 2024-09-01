package co.fineants.api.domain.kis.properties;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import co.fineants.api.domain.kis.properties.kiscodevalue.KisCodeValue;

public class KisHeaderBuilder {
	private final Map<KisHeader, String> headers = new EnumMap<>(KisHeader.class);

	public static KisHeaderBuilder builder() {
		return new KisHeaderBuilder();
	}

	public KisHeaderBuilder add(KisHeader header, String value) {
		headers.put(header, value);
		return this;
	}

	public KisHeaderBuilder add(KisHeader header, KisCodeValue value) {
		headers.put(header, value.getCode());
		return this;
	}

	public MultiValueMap<String, String> build() {
		return headers.entrySet().stream()
			.collect(LinkedMultiValueMap::new,
				(map, entry) -> map.add(entry.getKey().getHeaderName(), entry.getValue()),
				LinkedMultiValueMap::addAll);
	}
}
