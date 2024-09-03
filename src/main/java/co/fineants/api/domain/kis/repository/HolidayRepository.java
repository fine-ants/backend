package co.fineants.api.domain.kis.repository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.dividend.domain.dto.response.HolidayDto;
import co.fineants.api.domain.dividend.domain.reader.HolidayFileReader;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HolidayRepository {

	private final Set<LocalDate> holidays;

	public HolidayRepository(HolidayFileReader reader) {
		Set<LocalDate> temp = new HashSet<>();
		try {
			temp = reader.read().stream()
				.map(HolidayDto::getDate)
				.collect(Collectors.toSet());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		this.holidays = temp;
	}

	public boolean isHoliday(LocalDate localDate) {
		return holidays.contains(localDate);
	}
}
