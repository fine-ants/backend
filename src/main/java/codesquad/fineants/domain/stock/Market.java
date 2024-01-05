package codesquad.fineants.domain.stock;

import lombok.Getter;

@Getter
public enum Market {
	KOSPI("KOSPI"), KOSDAQ("KOSDAQ"), KONEX("KONEX"), KOSDAQ_GLOBAL("KOSDAQ GLOBAL");
	private final String name;

	Market(String name) {
		this.name = name;
	}
}
