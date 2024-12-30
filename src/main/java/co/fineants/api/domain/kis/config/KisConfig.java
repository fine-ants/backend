package co.fineants.api.domain.kis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.dividend.domain.reader.HolidayFileReader;
import co.fineants.api.domain.kis.repository.FileHolidayRepository;

@Configuration
public class KisConfig {

	@Bean
	public FileHolidayRepository fileHolidayRepository() {
		return new FileHolidayRepository(holidayFileReader());
	}

	@Bean
	public HolidayFileReader holidayFileReader() {
		return new HolidayFileReader();
	}
}
