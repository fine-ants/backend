package co.fineants.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.calculator.FileExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.reader.HolidayFileReader;
import co.fineants.api.domain.kis.repository.FileHolidayRepository;

@TestConfiguration
public class TestConfig {
	@Bean
	@Primary
	public ExDividendDateCalculator exDividendDateCalculator() {
		return new FileExDividendDateCalculator(new FileHolidayRepository(new HolidayFileReader()));
	}
}
