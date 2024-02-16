package codesquad.fineants.spring.api.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StockErrorCode implements ErrorCode {

	NOT_FOUND_STOCK(HttpStatus.NOT_FOUND, "종목을 찾을 수 없습니다"),
	NOT_FOUND_MARKET(HttpStatus.NOT_FOUND, "시장 종류를 찾을 수 없습니다"),
	BAD_REQUEST_TARGET_PRICE_NOTIFICATION_LIMIT(HttpStatus.BAD_REQUEST, "지정가 알림은 최대 5개까지 가능합니다"),
	BAD_REQUEST_TARGET_PRICE_NOTIFICATION_EXIST(HttpStatus.BAD_REQUEST, "해당 지정가는 이미 존재합니다"),
	BAD_REQUEST_STOCK_TARGET_PRICE_EXIST(HttpStatus.BAD_REQUEST, "해당 종목 지정가는 존재합니다"),
	NOT_FOUND_TARGET_PRICE(HttpStatus.NOT_FOUND, "존재하지 않은 지정가 알림입니다"),
	FORBIDDEN_DELETE_TARGET_PRICE_NOTIFICATION(HttpStatus.FORBIDDEN, "지정가 알림을 삭제할 권한이 없습니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "종목 에러 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
