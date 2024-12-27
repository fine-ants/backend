package co.fineants.api.domain.holiday.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.holiday.repository.HolidayRepository;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.domain.dto.response.KisHoliday;
import co.fineants.api.global.common.delay.DelayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayService {

	private final KisClient kisClient;
	private final DelayManager delayManager;
	private final HolidayRepository repository;

	@Transactional
	public List<Holiday> updateHoliday(LocalDate baseDate) {
		// 한국투자증권에 baseDate를 기준으로 기준일자 이후의 국내 휴장일을 조회합니다.
		List<Holiday> holidays = kisClient.fetchHolidays(baseDate)
			.map(kisHolidays -> kisHolidays.stream()
				.map(KisHoliday::toEntity)
				.toList())
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyList);

		// 국내 휴장일 조회된 데이터중에서 개장하지 않는 데이터를 필터링
		List<Holiday> closeHolidays = holidays.stream()
			.filter(Holiday::isCloseMarket)
			.toList();
		// 중복 데이터 삭제
		List<LocalDate> baseDates = closeHolidays.stream()
			.map(Holiday::getBaseDate)
			.toList();
		int deleted = repository.deleteAllByBaseDate(baseDates);
		log.info("delete count: {}", deleted);
		// 데이터 저장
		return repository.saveAll(closeHolidays).stream()
			.sorted()
			.toList();
	}
}
