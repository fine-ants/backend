package co.fineants.api.global.common.time;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
public class DefaultLocalDateTimeService implements LocalDateTimeService {
	@Override
	public LocalDate getLocalDateWithNow() {
		return LocalDate.now();
	}
}
