package co.fineants.api.domain.holiday.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.holiday.domain.entity.Holiday;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HolidayService {

	@Transactional
	public List<Holiday> updateHoliday(LocalDate baseDate) {
		List<Holiday> result = List.of(
			Holiday.close(LocalDate.of(2025, 1, 1)),
			Holiday.close(LocalDate.of(2025, 1, 28)),
			Holiday.close(LocalDate.of(2025, 1, 29)),
			Holiday.close(LocalDate.of(2025, 1, 30))
		);
		return result;
	}
}
