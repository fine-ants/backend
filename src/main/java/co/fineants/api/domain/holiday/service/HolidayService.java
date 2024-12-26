package co.fineants.api.domain.holiday.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.holiday.domain.entity.Holiday;

@Service
public class HolidayService {

	@Transactional
	public List<Holiday> updateHoliday(LocalDate baseDate) {
		return new ArrayList<>();
	}
}
