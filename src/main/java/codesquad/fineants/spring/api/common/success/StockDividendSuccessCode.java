package codesquad.fineants.spring.api.common.success;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public enum StockDividendSuccessCode implements SuccessCode {
	OK_INIT_DIVIDENDS(HttpStatus.OK, "배당 일정이 초기화되었습니다");

	private final HttpStatus httpStatus;
	private final String message;
}
