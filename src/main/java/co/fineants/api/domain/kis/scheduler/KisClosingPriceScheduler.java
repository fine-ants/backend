package co.fineants.api.domain.kis.scheduler;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.kis.repository.HolidayRepository;
import co.fineants.api.domain.kis.service.KisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KisClosingPriceScheduler {
	private final KisService kisService;
	private final HolidayRepository holidayRepository;

	// start pm 15:30
	@Scheduled(cron = "${cron.expression.closing-price:0 30 15 * * ?}")
	@Transactional(readOnly = true)
	public void scheduledRefreshAllClosingPrice() {
		if (holidayRepository.isHoliday(LocalDate.now())) {
			return;
		}
		kisService.refreshAllClosingPrice();
	}
}
