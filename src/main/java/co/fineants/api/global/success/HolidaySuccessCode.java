package co.fineants.api.global.success;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HolidaySuccessCode implements SuccessCode {
	UPDATE_HOLIDAYS(HttpStatus.OK, "국내 휴장 일정을 업데이트하였습니다");

	private final HttpStatus httpStatus;
	private final String message;
}
