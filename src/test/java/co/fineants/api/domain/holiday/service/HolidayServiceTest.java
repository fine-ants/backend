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
import co.fineants.api.domain.holiday.repository.HolidayRepository;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.domain.dto.response.KisHoliday;
import reactor.core.publisher.Mono;

class HolidayServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private HolidayService service;

	@Autowired
	private HolidayRepository repository;

	@MockBean
	private KisClient kisClient;

	@DisplayName("국내 휴장 일정을 수정한다")
	@Test
	void updateHoliday() {
		// given
		repository.save(Holiday.close(LocalDate.of(2025, 1, 1))); // 기존 데이터가 있다고 가정
		LocalDate baseDate = LocalDate.of(2024, 12, 26);
		List<KisHoliday> data = List.of(
			KisHoliday.open(LocalDate.of(2024, 12, 26)),
			KisHoliday.open(LocalDate.of(2024, 12, 27)),
			KisHoliday.close(LocalDate.of(2024, 12, 28)),
			KisHoliday.close(LocalDate.of(2024, 12, 29)),
			KisHoliday.open(LocalDate.of(2024, 12, 30)),
			KisHoliday.close(LocalDate.of(2024, 12, 31)),
			KisHoliday.close(LocalDate.of(2025, 1, 1)),
			KisHoliday.open(LocalDate.of(2025, 1, 2)),
			KisHoliday.open(LocalDate.of(2025, 1, 3)),
			KisHoliday.close(LocalDate.of(2025, 1, 4)),
			KisHoliday.close(LocalDate.of(2025, 1, 5)),
			KisHoliday.open(LocalDate.of(2025, 1, 6)),
			KisHoliday.open(LocalDate.of(2025, 1, 7)),
			KisHoliday.open(LocalDate.of(2025, 1, 8)),
			KisHoliday.open(LocalDate.of(2025, 1, 9)),
			KisHoliday.open(LocalDate.of(2025, 1, 10)),
			KisHoliday.close(LocalDate.of(2025, 1, 11)),
			KisHoliday.close(LocalDate.of(2025, 1, 12)),
			KisHoliday.open(LocalDate.of(2025, 1, 13)),
			KisHoliday.open(LocalDate.of(2025, 1, 14)),
			KisHoliday.open(LocalDate.of(2025, 1, 15)),
			KisHoliday.open(LocalDate.of(2025, 1, 16)),
			KisHoliday.open(LocalDate.of(2025, 1, 17)),
			KisHoliday.close(LocalDate.of(2025, 1, 18))
		);
		given(kisClient.fetchHolidays(baseDate))
			.willReturn(Mono.just(data));
		// when
		List<Holiday> actual = service.updateHoliday(baseDate);
		// then
		List<Holiday> expected = List.of(
			Holiday.close(LocalDate.of(2024, 12, 28)),
			Holiday.close(LocalDate.of(2024, 12, 29)),
			Holiday.close(LocalDate.of(2024, 12, 31)),
			Holiday.close(LocalDate.of(2025, 1, 1)),
			Holiday.close(LocalDate.of(2025, 1, 4)),
			Holiday.close(LocalDate.of(2025, 1, 5)),
			Holiday.close(LocalDate.of(2025, 1, 11)),
			Holiday.close(LocalDate.of(2025, 1, 12)),
			Holiday.close(LocalDate.of(2025, 1, 18))
		);
		Assertions.assertThat(actual)
			.hasSize(expected.size())
			.containsExactlyElementsOf(expected);
		Assertions.assertThat(repository.findAll())
			.hasSize(expected.size())
			.containsExactlyElementsOf(expected);
	}
}
