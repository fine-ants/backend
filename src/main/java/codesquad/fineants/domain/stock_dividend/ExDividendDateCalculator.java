package codesquad.fineants.domain.stock_dividend;

import java.time.DayOfWeek;
import java.time.LocalDate;

import codesquad.fineants.spring.api.kis.manager.HolidayManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExDividendDateCalculator {

	private final HolidayManager holidayManager;

	/**
	 * 배정기준일(recordDate) 기준 배당락일 계산
	 * @param recordDate 배정기준일
	 * @return 배당락일
	 */
	public LocalDate calculate(LocalDate recordDate) {
		LocalDate previousDay = recordDate.minusDays(1);

		while (isHolidayOrWeekend(previousDay)) {
			previousDay = previousDay.minusDays(1);
		}

		return previousDay;
	}

	/**
	 * 영업일 여부 확인
	 * @param localDate 일자
	 * @return 엽업일(주말 및 공휴일 제외) 여부
	 */
	private boolean isHolidayOrWeekend(LocalDate localDate) {
		DayOfWeek dayOfWeek = localDate.getDayOfWeek();
		return isWeekend(dayOfWeek) || holidayManager.isHoliday(localDate);
	}

	/**
	 * 주말 여부 확인
	 * @param dayOfWeek 요일
	 * @return 주말 여부
	 */
	private boolean isWeekend(DayOfWeek dayOfWeek) {
		return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
	}
}
