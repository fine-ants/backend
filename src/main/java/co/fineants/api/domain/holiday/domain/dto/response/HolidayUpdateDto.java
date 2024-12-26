package co.fineants.api.domain.holiday.domain.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.holiday.domain.entity.Holiday;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
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

	public static HolidayUpdateDto close(LocalDate baseDate) {
		return new HolidayUpdateDto(baseDate, baseDate.getDayOfWeek(), Boolean.FALSE);
	}

	public static HolidayUpdateDto from(Holiday holiday) {
		return new HolidayUpdateDto(holiday.getBaseDate(), holiday.getBaseDate().getDayOfWeek(),
			holiday.isOpenMarket());
	}

	@Override
	public String toString() {
		return String.format("국내 휴장 일정 업데이트(기준일자=%s, 요일명=%s, 개장여부=%s)", baseDate, dayOfWeek, isOpen);
	}
}
