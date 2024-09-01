package co.fineants.api.global.common.time;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface LocalDateTimeService {
	default LocalDate getLocalDateWithNow() {
		return LocalDate.now();
	}

	default LocalDateTime getLocalDateTimeWithNow() {
		return LocalDateTime.now();
	}
}
