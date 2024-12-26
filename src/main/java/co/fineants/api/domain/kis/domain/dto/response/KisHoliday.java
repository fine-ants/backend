package co.fineants.api.domain.kis.domain.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.holiday.domain.entity.Holiday;

public class KisHoliday {

	@JsonProperty("baseDate")
	private final LocalDate baseDate;

	@JsonProperty("isOpen")
	private final Boolean isOpen;

	@JsonCreator
	public KisHoliday(
		@JsonProperty("bass_dt") LocalDate baseDate,
		@JsonProperty("opnd_yn") Boolean isOpen) {
		this.baseDate = baseDate;
		this.isOpen = isOpen;
	}

	public static KisHoliday open(LocalDate baseDate) {
		return new KisHoliday(baseDate, true);
	}

	public static KisHoliday close(LocalDate baseDate) {
		return new KisHoliday(baseDate, false);
	}

	public Holiday toEntity() {
		return Holiday.of(baseDate, isOpen);
	}
}
