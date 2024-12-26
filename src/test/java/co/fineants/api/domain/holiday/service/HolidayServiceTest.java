package co.fineants.api.domain.holiday.service;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.holiday.domain.entity.Holiday;

class HolidayServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private HolidayService service;

	@DisplayName("국내 휴장 일정을 수정한다")
	@Test
	void updateHoliday() {
		// given
		LocalDate baseDate = LocalDate.of(2024, 12, 26);
		// when
		List<Holiday> actual = service.updateHoliday(baseDate);
		// then
		List<Holiday> expected = List.of(
			Holiday.close(LocalDate.of(2025, 1, 1)),
			Holiday.close(LocalDate.of(2025, 1, 28)),
			Holiday.close(LocalDate.of(2025, 1, 29)),
			Holiday.close(LocalDate.of(2025, 1, 30))
		);
		Assertions.assertThat(actual).containsExactlyElementsOf(expected);
	}
}
