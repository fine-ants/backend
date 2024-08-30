package codesquad.fineants.domain.kis.repository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.dividend.domain.dto.response.HolidayDto;
import codesquad.fineants.domain.dividend.domain.reader.HolidayFileReader;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HolidayRepository {

	private final Set<LocalDate> holidays;

	public HolidayRepository(HolidayFileReader reader) throws IOException {
		this.holidays = reader.read().stream()
			.map(HolidayDto::getDate)
			.collect(Collectors.toSet());
	}

	public boolean isHoliday(LocalDate localDate) {
		return holidays.contains(localDate);
	}
}
