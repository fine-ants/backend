package codesquad.fineants.global.success;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisSuccessCode implements SuccessCode {
	OK_REFRESH_CURRENT_PRICE_STOCKS(HttpStatus.OK, "종목 현재가가 갱신되었습니다"),
	OK_REFRESH_LAST_DAY_CLOSING_PRICE(HttpStatus.OK, "종목 종가가 갱신되었습니다"),
	OK_FETCH_CURRENT_PRICE(HttpStatus.OK, "종목 현재가가 조회되었습니다"),
	OK_FETCH_DIVIDEND(HttpStatus.OK, "배당금이 조회되었습니다"),
	OK_REFRESH_DIVIDEND(HttpStatus.OK, "배당일정이 갱신되었습니다"),
	OK_FETCH_IPO_SEARCh_STOCK_INFO(HttpStatus.OK, "상장된 종목들의 정보가 조회되었습니다");

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
