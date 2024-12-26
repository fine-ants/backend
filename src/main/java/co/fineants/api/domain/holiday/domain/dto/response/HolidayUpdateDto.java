package co.fineants.api.domain.holiday.domain.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HolidayUpdateDto {
	@JsonProperty("baseDate")
	private final LocalDate baseDate;
	@JsonProperty("dayOfWeek")
	private final DayOfWeek dayOfWeek;
	@JsonProperty("isOpen")
	private final Boolean isOpen;

	@JsonCreator
	private HolidayUpdateDto(
		@JsonProperty("baseDate") LocalDate baseDate,
		@JsonProperty("dayOfWeek") DayOfWeek dayOfWeek,
		@JsonProperty("isOpen") Boolean isOpen) {
		this.baseDate = baseDate;
		this.dayOfWeek = dayOfWeek;
		this.isOpen = isOpen;
	}

	public static HolidayUpdateDto open(LocalDate baseDate) {
		return new HolidayUpdateDto(baseDate, baseDate.getDayOfWeek(), Boolean.TRUE);
	}
}
