package codesquad.fineants.domain.kis.service;

import java.time.LocalDate;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.kis.aop.CheckedKisAccessToken;
import codesquad.fineants.domain.kis.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile(value = "production")
@Slf4j
@RequiredArgsConstructor
@Service
public class KisProductionScheduledService {

	private final HolidayRepository holidayRepository;
	private final KisService kisService;

	@Scheduled(cron = "0/5 * 9-15 ? * MON,TUE,WED,THU,FRI")
	@Transactional
	@CheckedKisAccessToken
	public void refreshCurrentPrice() {
		// 휴장일인 경우 실행하지 않음
		if (holidayRepository.isHoliday(LocalDate.now())) {
			return;
		}
		kisService.refreshAllStockCurrentPrice();
	}

}
