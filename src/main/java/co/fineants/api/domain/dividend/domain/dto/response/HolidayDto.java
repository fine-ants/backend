package co.fineants.api.domain.dividend.domain.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HolidayDto {

	private LocalDate date;
	private String note;
}
