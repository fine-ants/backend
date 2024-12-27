package co.fineants.api.domain.kis.domain.dto.response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.holiday.domain.entity.Holiday;
import lombok.EqualsAndHashCode;

// TODO: 12/26/24 refactoring 
@EqualsAndHashCode
public class KisHoliday {

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

	@JsonProperty("baseDate")
	private final String baseDate; // YYYYMMDD

	@JsonProperty("isOpen")
	private final String isOpen; // "Y" or "N"

	@JsonCreator
	public KisHoliday(
		@JsonProperty("bass_dt") String baseDate,
		@JsonProperty("opnd_yn") String isOpen) {
		this.baseDate = baseDate;
		this.isOpen = isOpen;
	}

	public static KisHoliday open(LocalDate baseDate) {
		return new KisHoliday(toBasicIsoDate(baseDate), "Y");
	}

	public static KisHoliday close(LocalDate baseDate) {
		return new KisHoliday(toBasicIsoDate(baseDate), "N");
	}

	@NotNull
	private static String toBasicIsoDate(LocalDate baseDate) {
		return baseDate.format(FORMAT);
	}

	public Holiday toEntity() {
		LocalDate localBaseDate = LocalDate.parse(baseDate, FORMAT);
		boolean isOpenValue = isOpen.equals("Y");
		return Holiday.of(localBaseDate, isOpenValue);
	}

	@Override
	public String toString() {
		return String.format("한국투자증권 휴장 조회결과(기준일자=%s, 개장여부=%s)", baseDate, isOpen);
	}
}
