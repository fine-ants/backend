package co.fineants.api.domain.holiday.service;

import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.domain.dto.response.KisHoliday;
import reactor.core.publisher.Mono;

class HolidayServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private HolidayService service;

	@MockBean
	private KisClient kisClient;

	@DisplayName("국내 휴장 일정을 수정한다")
	@Test
	void updateHoliday() {
		// given
		LocalDate baseDate = LocalDate.of(2024, 12, 26);
		List<KisHoliday> data = List.of(
			KisHoliday.close(LocalDate.of(2025, 1, 1)),
			KisHoliday.open(LocalDate.of(2025, 1, 2)),
			KisHoliday.close(LocalDate.of(2025, 1, 28)),
			KisHoliday.close(LocalDate.of(2025, 1, 29)),
			KisHoliday.close(LocalDate.of(2025, 1, 30))
		);
		given(kisClient.fetchHolidays(baseDate))
			.willReturn(Mono.just(data));
		// when
		List<Holiday> actual = service.updateHoliday(baseDate);
		// then
		List<Holiday> expected = List.of(
			Holiday.close(LocalDate.of(2025, 1, 1)),
			Holiday.close(LocalDate.of(2025, 1, 28)),
			Holiday.close(LocalDate.of(2025, 1, 29)),
			Holiday.close(LocalDate.of(2025, 1, 30))
		);
		Assertions.assertThat(actual)
			.hasSize(4)
			.containsExactlyElementsOf(expected);
	}
}
