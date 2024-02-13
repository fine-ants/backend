package codesquad.fineants.spring.api.success.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StockSuccessCode implements SuccessCode {
	OK_SEARCH_STOCKS(HttpStatus.OK, "종목 검색이 완료되었습니다"),
	OK_SEARCH_DETAIL_STOCK(HttpStatus.OK, "종목 상세정보 조회가 완료되었습니다"),
	OK_CREATE_TARGET_PRICE_NOTIFICATION(HttpStatus.CREATED, "해당 종목 지정가 알림을 추가했습니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "종목 성공 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
