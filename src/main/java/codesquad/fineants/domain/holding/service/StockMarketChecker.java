package codesquad.fineants.domain.holding.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

@Component
public class StockMarketChecker {
	private static final LocalTime MARKET_OPEN_TIME = LocalTime.of(9, 0);
	private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(15, 30);

	// 주식 시장이 열려 있는지 확인한다
	public boolean isMarketOpen(LocalDateTime dateTime) {
		DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
		LocalTime currentTime = dateTime.toLocalTime();

		if (isWeekend(dayOfWeek)) {
			return false;
		}

		// 평일인 경우 개장 시간과 폐장 시간 사이에 있는지 확인한다
		return currentTime.isAfter(MARKET_OPEN_TIME) && currentTime.isBefore(MARKET_CLOSE_TIME);
	}

	private boolean isWeekend(DayOfWeek dayOfWeek) {
		return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
	}
}
