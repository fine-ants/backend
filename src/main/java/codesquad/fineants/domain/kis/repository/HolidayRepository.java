package codesquad.fineants.domain.kis.repository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.dividend.domain.dto.response.HolidayDto;
import codesquad.fineants.domain.dividend.domain.reader.HolidayFileReader;

@Component
public class HolidayRepository {

	private final Set<LocalDate> holidays;

	public HolidayRepository(HolidayFileReader reader) {
		try {
			this.holidays = reader.read().stream()
				.map(HolidayDto::getDate)
				.collect(Collectors.toSet());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isHoliday(LocalDate localDate) {
		return holidays.contains(localDate);
	}
}
