package co.fineants.api.domain.holiday.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.holiday.domain.dto.response.HolidayUpdateDto;
import co.fineants.api.domain.holiday.service.HolidayService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.success.HolidaySuccessCode;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HolidayRestController {

	private final HolidayService service;

	@PutMapping("/api/holidays")
	@Secured("ROLE_ADMIN")
	public ApiResponse<List<HolidayUpdateDto>> updateHoliday(
		@RequestParam(required = false) @DateTimeFormat(pattern = "yyyyMMdd") LocalDate baseDate) {
		if (baseDate == null) {
			baseDate = LocalDate.now();
		}
		List<HolidayUpdateDto> data = service.updateHoliday(baseDate).stream()
			.map(HolidayUpdateDto::from)
			.toList();
		return ApiResponse.success(HolidaySuccessCode.UPDATE_HOLIDAYS, data);
	}
}
