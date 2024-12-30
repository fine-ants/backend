package co.fineants.api.domain.dividend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.calculator.MySqlExDividendDateCalculator;
import co.fineants.api.domain.holiday.service.HolidayService;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DividendConfig {

	private final HolidayService service;

	@Bean
	public ExDividendDateCalculator exDividendDateCalculator() {
		return new MySqlExDividendDateCalculator(service);
	}
}
