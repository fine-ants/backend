package codesquad.fineants.spring.api.kis.manager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.stock_dividend.HolidayDto;
import codesquad.fineants.spring.api.stock_dividend.HolidayFileReader;

@Component
public class HolidayManager {

	private final Set<LocalDate> holidays;

	public HolidayManager(HolidayFileReader reader) {
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
