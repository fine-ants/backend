package co.fineants.api.domain.holiday.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.holiday.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class HolidayScheduler {
	private final HolidayService service;

	/**
	 * 정오(0시 0분 0초)에 한번씩 국내 휴장 일정을 업데이트한다
	 * <p>
	 * 기준일자(당일)을 기준으로 기준일자를 포함한 24일까지의 일저중 휴장 일정만을 저장합니다
	 * 저장소에는 휴장하는 일정만 저장됩니다.
	 * </p>
	 */
	@Scheduled(cron = "${cron.expression.update-holidays:0 0 0 * * ?}")
	@Transactional
	public void updateHolidays() {
		List<Holiday> holidays = service.updateHoliday(LocalDate.now());
		log.info("update holidays : {}", holidays);
	}
}
