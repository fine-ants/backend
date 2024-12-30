package co.fineants.api.domain.kis.scheduler;

import java.time.LocalDate;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.holiday.service.HolidayService;
import co.fineants.api.domain.kis.service.KisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile(value = "production")
@Slf4j
@RequiredArgsConstructor
@Service
public class KisProductionScheduler {

	private final HolidayService holidayService;
	private final KisService kisService;

	@Scheduled(cron = "0/5 * 9-16 ? * MON,TUE,WED,THU,FRI")
	@Transactional
	public void refreshCurrentPrice() {
		if (holidayService.isHoliday(LocalDate.now())) {
			return;
		}
		kisService.refreshAllStockCurrentPrice();
	}
}
